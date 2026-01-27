package com.aspd.backend.controller;

import com.aspd.backend.dto.AssignmentComparisonResult;
import com.aspd.backend.dto.AssignmentDto;
import com.aspd.backend.dto.ReoptimizationRequest;
import com.aspd.backend.dto.ReoptimizationResponse;
import com.aspd.backend.mapper.AssignmentMapper;
import com.aspd.backend.model.AssignmentStatus;
import com.aspd.backend.model.InternshipAssignment;
import com.aspd.backend.model.PlannedInternship;
import com.aspd.backend.model.StudentInternshipDemand;
import com.aspd.backend.optimization.OptimizationJob;
import com.aspd.backend.optimization.JobType;
import com.aspd.backend.optimization.OptimizationJobService;
import com.aspd.backend.optimization.OptimizationJob;
import com.aspd.backend.optimization.JobType;
import com.aspd.backend.repository.InternshipAssignmentRepository;
import com.aspd.backend.service.ReoptimizationService;
import com.aspd.backend.solver.StudentAssignmentSolution;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * REST API for re-optimization functionality.
 * Allows re-optimizing student assignments for a new semester while preserving
 * valid assignments from the previous semester.
 */
@Slf4j
@RestController
@RequestMapping("/api/reoptimization")
@RequiredArgsConstructor
public class ReoptimizationController {

    private final ReoptimizationService reoptimizationService;
    private final OptimizationJobService jobService;
    private final AssignmentMapper assignmentMapper;
    private final InternshipAssignmentRepository assignmentRepository;

    /**
     * Re-optimize student assignments for a new semester.
     * 
     * Example: When moving from winter to summer semester, this endpoint will:
     * 1. Load winter semester baseline assignments (automatically determined)
     * 2. Re-optimize for summer taking into account configuration changes
     * 3. Preserve pinned assignments and prefer keeping other baseline assignments
     * 4. Save results as new baseline for summer
     * 
     * @param request Contains school year with semester notation (e.g., "SoSe2025") and optional time budget
     * @return Re-optimization results with assignments and statistics
     */
    @PreAuthorize("hasAnyAuthority('EDIT')")
    @PostMapping("/optimize")
    public ResponseEntity<ReoptimizationResponse> reoptimize(
            @RequestBody ReoptimizationRequest request) {
        
        log.info("Re-optimization request received: year={}", request.getSchoolYear());
        
        // Validate request
        if (request.getSchoolYear() == null || request.getSchoolYear().isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        
        // Validate semester format
        if (!request.getSchoolYear().startsWith("WiSe") && 
            !request.getSchoolYear().startsWith("SoSe")) {
            log.error("Invalid semester format: {}. Expected 'WiSe2025' or 'SoSe2025'", 
                request.getSchoolYear());
            return ResponseEntity.badRequest().build();
        }
        
        // Run re-optimization
        StudentAssignmentSolution solution = reoptimizationService.reoptimize(
            request.getSchoolYear(),
            request.getTimeBudget(),
            request.getUncompletedInternships()
        );
        
        // Convert to response DTO
        ReoptimizationResponse response = buildResponse(solution, request);
        
        // Clear budget info from ThreadLocal after building response
        reoptimizationService.clearBudgetInfo();
        
        log.info("Re-optimization completed: {} students assigned, score: {}", 
            response.getStudentsAssigned(), response.getScore());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Re-optimize student assignments asynchronously.
     * Returns immediately with a job ID that can be polled for status.
     * 
     * @param request Contains school year, time budget, and Phase1 solver time limit
     * @return Job details with job ID for polling
     */
    @PreAuthorize("hasAnyAuthority('EDIT')")
    @PostMapping("/optimize-async")
    public ResponseEntity<OptimizationJob> reoptimizeAsync(
            @RequestBody ReoptimizationRequest request) {
        
        log.info("Async re-optimization request received: year={}, phase1TimeLimitSeconds={}", 
                request.getSchoolYear(), request.getPhase1TimeLimitSeconds());
        
        // Validate request
        if (request.getSchoolYear() == null || request.getSchoolYear().isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        
        // Validate semester format
        if (!request.getSchoolYear().startsWith("WiSe") && 
            !request.getSchoolYear().startsWith("SoSe")) {
            log.error("Invalid semester format: {}. Expected 'WiSe2025' or 'SoSe2025'", 
                request.getSchoolYear());
            return ResponseEntity.badRequest().build();
        }
        
        // Validate phase1TimeLimitSeconds (default 300 if not provided)
        Long phase1TimeLimit = request.getPhase1TimeLimitSeconds() != null 
                ? request.getPhase1TimeLimitSeconds() 
                : 300L;
        
        if (phase1TimeLimit < 60 || phase1TimeLimit > 43200) {
            log.error("Invalid Phase 1 solver time limit: {}. Must be between 60 and 43200 seconds", phase1TimeLimit);
            return ResponseEntity.badRequest().build();
        }
        
        // Create or get existing job (prevents duplicate concurrent jobs for same schoolYear+type)
        // Inflate by 120s to account for Phase 2 fixed runtime so frontend ETA covers both phases
        int totalTimeSeconds = Math.toIntExact(phase1TimeLimit + 120);
        OptimizationJob job = jobService.createOrGetExistingJob(
            JobType.REOPTIMIZATION, request.getSchoolYear(), totalTimeSeconds);
        
        if (job.getStatus() != com.aspd.backend.optimization.JobStatus.QUEUED) {
            // Job already exists and is running or completed
            log.info("Job already exists with status: {}", job.getStatus());
            return ResponseEntity.ok(job);
        }
        
        // Start async re-optimization
        reoptimizationService.reoptimizeAsync(
                request.getSchoolYear(),
                request.getTimeBudget(),
                request.getUncompletedInternships(),
                phase1TimeLimit,
                job.getJobId());
        
        log.info("Async re-optimization started with job ID: {}", job.getJobId());
        return ResponseEntity.ok(job);
    }

    /**
     * Build response DTO from optimization solution.
     */
    private ReoptimizationResponse buildResponse(
            StudentAssignmentSolution solution,
            ReoptimizationRequest request) {
        
        List<StudentInternshipDemand> demands = solution.getStudentDemands();
        
        // Count assignments
        long assigned = demands.stream()
            .filter(d -> d.getAssignedInternship() != null)
            .count();
        
        // Count pinned
        long pinned = demands.stream()
            .filter(StudentInternshipDemand::isPinned)
            .count();
        
        // Convert demands to final assignments, then to DTOs
        List<InternshipAssignment> finalAssignments = demands.stream()
            .filter(d -> d.getAssignedInternship() != null)
            .filter(d -> d.getAssignedInternship().getAssignedTeacher() != null)
            .filter(d -> d.getAssignedInternship().getSchool() != null)
            .map(d -> buildInternshipAssignment(d, request.getSchoolYear()))
            .collect(Collectors.toList());
        
        List<com.aspd.backend.dto.AssignmentDto> assignments = finalAssignments.stream()
            .map(assignmentMapper::toDto)
            .collect(Collectors.toList());
        
        String semester = request.getSchoolYear().startsWith("WiSe") ? "Winter" : "Summer";
        String year = request.getSchoolYear().substring(4);
        
        String message = String.format(
            "Re-optimization completed for %s %s. " +
            "%d/%d students assigned (%d pinned).",
            semester,
            year,
            assigned,
            demands.size(),
            pinned
        );
        
        return ReoptimizationResponse.builder()
            .schoolYear(request.getSchoolYear())
            .score(solution.getScore() != null ? solution.getScore().toString() : "0hard/0soft")
            .studentsAssigned((int) assigned)
            .assignmentsPinned((int) pinned)
            .totalDemands(demands.size())
            .assignments(assignments)
            .message(message)
            .winterBudgetUsed(reoptimizationService.getWinterBudgetUsed())
            .initialBudget(reoptimizationService.getInitialBudget())
            .finalBudget(reoptimizationService.getFinalBudget())
            .build();
    }
    
    /**
     * Build InternshipAssignment from StudentInternshipDemand.
     */
    private InternshipAssignment buildInternshipAssignment(
            StudentInternshipDemand demand,
            String schoolYear) {
        
        PlannedInternship internship = demand.getAssignedInternship();
        
        return InternshipAssignment.builder()
            .studentConfig(demand.getStudentConfig())
            .plannedInternship(internship)
            .teacher(internship.getAssignedTeacher())
            .school(internship.getSchool())
            .praktikumType(internship.getPraktikumType())
            .course(internship.getCourse())
            .schoolYear(schoolYear)
            .status(AssignmentStatus.PROPOSED)
            .build();
    }
    
    /**
     * Compare assignments between two semesters (baseline vs target).
     * Shows which assignments were preserved and which changed.
     * 
     * @param baselineSemester Baseline semester (e.g., "WiSe2025")
     * @param targetSemester Target semester (e.g., "SoSe2025")
     * @return Comparison results with statistics
     */
    @PreAuthorize("hasAnyAuthority('VIEW')")
    @GetMapping("/compare")
    public ResponseEntity<AssignmentComparisonResult> compareAssignments(
            @RequestParam String baselineSemester,
            @RequestParam String targetSemester) {
        
        log.info("Comparing assignments: {} vs {}", baselineSemester, targetSemester);
        
        // Load assignments from both semesters
        List<InternshipAssignment> baselineAssignments = assignmentRepository.findBySchoolYear(baselineSemester);
        List<InternshipAssignment> targetAssignments = assignmentRepository.findBySchoolYear(targetSemester);
        
        if (baselineAssignments.isEmpty() || targetAssignments.isEmpty()) {
            log.warn("Missing assignments for comparison. Baseline: {}, Target: {}", 
                baselineAssignments.size(), targetAssignments.size());
            return ResponseEntity.badRequest().build();
        }
        
        // Build comparison
        AssignmentComparisonResult result = compareAssignmentLists(
            baselineAssignments, targetAssignments, baselineSemester, targetSemester);
        
        log.info("Comparison complete: {} preserved, {} changed", 
            result.getAssignmentsPreserved(), result.getAssignmentsChanged());
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * Compare two lists of assignments and generate detailed comparison.
     */
    private AssignmentComparisonResult compareAssignmentLists(
            List<InternshipAssignment> baselineAssignments,
            List<InternshipAssignment> targetAssignments,
            String baselineSemester,
            String targetSemester) {
        
        List<AssignmentComparisonResult.AssignmentComparison> comparisons = new ArrayList<>();
        int preserved = 0;
        int changed = 0;
        
        // Compare each target assignment with baseline
        for (InternshipAssignment target : targetAssignments) {
            String studentName = target.getStudentConfig().getStudent().getFirstName() + " " +
                               target.getStudentConfig().getStudent().getLastName();
            String praktikumType = target.getPraktikumType().toString();
            
            // Find matching baseline assignment (same student + same praktikum type)
            InternshipAssignment baseline = findMatchingBaseline(
                baselineAssignments,
                target.getStudentConfig().getStudent().getMatriculationNbr(),
                target.getPraktikumType()
            );
            
            if (baseline == null) {
                // No baseline assignment found - this is a new assignment
                comparisons.add(AssignmentComparisonResult.AssignmentComparison.builder()
                    .studentName(studentName)
                    .praktikumType(praktikumType)
                    .preserved(false)
                    .baselineTeacher(null)
                    .baselineSchool(null)
                    .baselineCourse(null)
                    .targetTeacher(getTeacherName(target))
                    .targetSchool(getSchoolName(target))
                    .targetCourse(getCourseName(target))
                    .changeReason("New assignment (no baseline)")
                    .build());
                changed++;
            } else {
                // Check if assignment was preserved
                boolean isPreserved = isAssignmentPreserved(baseline, target);
                
                comparisons.add(AssignmentComparisonResult.AssignmentComparison.builder()
                    .studentName(studentName)
                    .praktikumType(praktikumType)
                    .preserved(isPreserved)
                    .baselineTeacher(getTeacherName(baseline))
                    .baselineSchool(getSchoolName(baseline))
                    .baselineCourse(getCourseName(baseline))
                    .targetTeacher(getTeacherName(target))
                    .targetSchool(getSchoolName(target))
                    .targetCourse(getCourseName(target))
                    .changeReason(isPreserved ? "Preserved" : getChangeReason(baseline, target))
                    .build());
                
                if (isPreserved) {
                    preserved++;
                } else {
                    changed++;
                }
            }
        }
        
        double preservationRate = targetAssignments.isEmpty() ? 0.0 :
            (double) preserved / targetAssignments.size() * 100.0;
        
        return AssignmentComparisonResult.builder()
            .baselineSemester(baselineSemester)
            .targetSemester(targetSemester)
            .totalBaselineAssignments(baselineAssignments.size())
            .totalTargetAssignments(targetAssignments.size())
            .assignmentsPreserved(preserved)
            .assignmentsChanged(changed)
            .preservationRate(preservationRate)
            .comparisons(comparisons)
            .build();
    }
    
    private InternshipAssignment findMatchingBaseline(
            List<InternshipAssignment> baselineAssignments,
            int studentId,
            com.aspd.backend.model.PraktikumType praktikumType) {
        
        return baselineAssignments.stream()
            .filter(a -> a.getStudentConfig().getStudent().getMatriculationNbr() == studentId)
            .filter(a -> a.getPraktikumType() == praktikumType)
            .findFirst()
            .orElse(null);
    }
    
    private boolean isAssignmentPreserved(InternshipAssignment baseline, InternshipAssignment target) {
        // Check if teacher-school-course combination is the same
        Long baselineTeacherId = baseline.getTeacher() != null ? baseline.getTeacher().getTeacherId() : null;
        Long targetTeacherId = target.getTeacher() != null ? target.getTeacher().getTeacherId() : null;
        
        Long baselineSchoolId = baseline.getSchool() != null ? baseline.getSchool().getId() : null;
        Long targetSchoolId = target.getSchool() != null ? target.getSchool().getId() : null;
        
        Long baselineCourseId = baseline.getCourse() != null ? baseline.getCourse().getId() : null;
        Long targetCourseId = target.getCourse() != null ? target.getCourse().getId() : null;
        
        return java.util.Objects.equals(baselineTeacherId, targetTeacherId) &&
               java.util.Objects.equals(baselineSchoolId, targetSchoolId) &&
               java.util.Objects.equals(baselineCourseId, targetCourseId);
    }
    
    private String getChangeReason(InternshipAssignment baseline, InternshipAssignment target) {
        List<String> changes = new ArrayList<>();
        
        Long baselineTeacherId = baseline.getTeacher() != null ? baseline.getTeacher().getTeacherId() : null;
        Long targetTeacherId = target.getTeacher() != null ? target.getTeacher().getTeacherId() : null;
        
        if (!java.util.Objects.equals(baselineTeacherId, targetTeacherId)) {
            changes.add("Teacher changed");
        }
        
        Long baselineSchoolId = baseline.getSchool() != null ? baseline.getSchool().getId() : null;
        Long targetSchoolId = target.getSchool() != null ? target.getSchool().getId() : null;
        
        if (!java.util.Objects.equals(baselineSchoolId, targetSchoolId)) {
            changes.add("School changed");
        }
        
        Long baselineCourseId = baseline.getCourse() != null ? baseline.getCourse().getId() : null;
        Long targetCourseId = target.getCourse() != null ? target.getCourse().getId() : null;
        
        if (!java.util.Objects.equals(baselineCourseId, targetCourseId)) {
            changes.add("Course changed");
        }
        
        return changes.isEmpty() ? "Unknown" : String.join(", ", changes);
    }
    
    private String getTeacherName(InternshipAssignment assignment) {
        if (assignment.getTeacher() == null) return null;
        return assignment.getTeacher().getFirstName() + " " + assignment.getTeacher().getLastName();
    }
    
    private String getSchoolName(InternshipAssignment assignment) {
        return assignment.getSchool() != null ? assignment.getSchool().getName() : null;
    }
    
    private String getCourseName(InternshipAssignment assignment) {
        return assignment.getCourse() != null ? assignment.getCourse().getName() : null;
    }
}
