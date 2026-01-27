package com.aspd.backend.controller;

import com.aspd.backend.dto.StudentAssignmentResult;
import com.aspd.backend.mapper.AssignmentMapper;
import com.aspd.backend.model.InternshipAssignment;
import com.aspd.backend.model.StudentConfig;
import com.aspd.backend.optimization.OptimizationJob;
import com.aspd.backend.optimization.JobType;
import com.aspd.backend.optimization.OptimizationJobService;
import com.aspd.backend.optimization.OptimizationJob;
import com.aspd.backend.optimization.JobType;
import com.aspd.backend.repository.InternshipAssignmentRepository;
import com.aspd.backend.repository.StudentConfigRepository;
import com.aspd.backend.service.Phase2OptimizationService;
import com.aspd.backend.solver.StudentAssignmentSolution;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller for Phase 2 optimization: Student assignment to internships.
 */
@Slf4j
@RestController
@RequestMapping("/api/internships/phase2")
public class Phase2Controller {

    private final Phase2OptimizationService phase2OptimizationService;
    private final OptimizationJobService jobService;
    private final StudentConfigRepository studentConfigRepository;
    private final InternshipAssignmentRepository assignmentRepository;
    private final AssignmentMapper assignmentMapper;

    public Phase2Controller(
            Phase2OptimizationService phase2OptimizationService,
            OptimizationJobService jobService,
            StudentConfigRepository studentConfigRepository,
            InternshipAssignmentRepository assignmentRepository,
            AssignmentMapper assignmentMapper) {
        this.phase2OptimizationService = phase2OptimizationService;
        this.jobService = jobService;
        this.studentConfigRepository = studentConfigRepository;
        this.assignmentRepository = assignmentRepository;
        this.assignmentMapper = assignmentMapper;
    }

    /**
     * Phase 2: Optimize student assignments to internships.
     * This assigns students to the planned internships created in Phase 1.
     */
    @PreAuthorize("hasAnyAuthority('EDIT')")
    @PostMapping("/optimize")
    public ResponseEntity<StudentAssignmentResult> optimize(
            @RequestParam(name = "schoolYear") String schoolYear) {
        log.info("Received Phase 2 optimization request for year: {}", schoolYear);

        // Load and validate student configs
        List<StudentConfig> studentConfigs = loadStudentConfigs(schoolYear);
        if (studentConfigs.isEmpty()) {
            log.error("NO STUDENT CONFIGS FOUND FOR YEAR: {}", schoolYear);
            return ResponseEntity.badRequest().build();
        }

        // Delete existing assignments to avoid duplicates
        deleteExistingAssignments(schoolYear);

        // Run Phase 2 optimization
        StudentAssignmentSolution phase2Solution = phase2OptimizationService.optimize(
                studentConfigs, schoolYear, 70);

        // Create and save final assignments
        List<InternshipAssignment> finalAssignments = phase2OptimizationService.createFinalAssignments(
                phase2Solution.getStudentDemands(), schoolYear);
        assignmentRepository.saveAll(finalAssignments);

        // Build and return response
        StudentAssignmentResult result = buildAssignmentResult(phase2Solution, finalAssignments, schoolYear);
        
        log.info("Phase 2 complete: {}/{} students assigned",
                result.getAssignedStudents(), result.getTotalStudents());

        return ResponseEntity.ok(result);
    }

    /**
     * Phase 2 Async: Start an asynchronous student assignment job.
     * Returns immediately with a job ID that can be polled for status.
     * Fixed time limit of 120 seconds.
     * 
     * @param schoolYear Academic year (e.g., "WiSe2024")
     * @return Job details with job ID for polling
     */
    @PreAuthorize("hasAnyAuthority('EDIT')")
    @PostMapping("/optimize-async")
    public ResponseEntity<OptimizationJob> optimizeAsync(
            @RequestParam(name = "schoolYear") String schoolYear) {
        
        log.info("Received Phase 2 ASYNC optimization request for year: {} (fixed 120s)", schoolYear);

        // Load and validate student configs
        List<StudentConfig> studentConfigs = loadStudentConfigs(schoolYear);
        if (studentConfigs.isEmpty()) {
            log.error("NO STUDENT CONFIGS FOUND FOR YEAR: {}", schoolYear);
            return ResponseEntity.badRequest().build();
        }

        // Delete existing assignments to avoid duplicates
        deleteExistingAssignments(schoolYear);

        // Check if Phase 1 is currently running for this schoolYear - prevent concurrent phases
        OptimizationJob phase1Job = jobService.getJobIfExists(JobType.PHASE1, schoolYear);
        if (phase1Job != null && phase1Job.getStatus() == com.aspd.backend.optimization.JobStatus.RUNNING) {
            log.warn("Cannot start Phase 2: Phase 1 is currently running for schoolYear={}", schoolYear);
            return ResponseEntity.status(409)
                    .body(OptimizationJob.builder()
                            .message("Phase 1 is currently running. Wait for it to complete before starting Phase 2.")
                            .status(com.aspd.backend.optimization.JobStatus.RUNNING)
                            .schoolYear(schoolYear)
                            .build());
        }

        // Create or get existing job (prevents duplicate concurrent jobs for same schoolYear+type)
        // Fixed 120 seconds for Phase 2
        OptimizationJob job = jobService.createOrGetExistingJob(JobType.PHASE2, schoolYear, 120);
        
        if (job.getStatus() != com.aspd.backend.optimization.JobStatus.QUEUED) {
            // Job already exists and is running or completed
            log.info("Job already exists with status: {}", job.getStatus());
            return ResponseEntity.ok(job);
        }

        // Start async optimization
        phase2OptimizationService.optimizeAsync(studentConfigs, schoolYear, 70, job.getJobId());
        
        log.info("Phase 2 async optimization started with job ID: {}", job.getJobId());
        return ResponseEntity.ok(job);
    }

    private List<StudentConfig> loadStudentConfigs(String schoolYear) {
        // Use JOIN FETCH to eagerly load Student entities to avoid lazy loading issues
        return studentConfigRepository.findByYearWithStudent(schoolYear);
    }

    private void deleteExistingAssignments(String schoolYear) {
        List<InternshipAssignment> existingAssignments = assignmentRepository.findBySchoolYear(schoolYear);
        if (!existingAssignments.isEmpty()) {
            log.info("Deleting {} existing assignments for year {}", existingAssignments.size(), schoolYear);
            assignmentRepository.deleteAll(existingAssignments);
        }
    }

    private StudentAssignmentResult buildAssignmentResult(
            StudentAssignmentSolution phase2Solution,
            List<InternshipAssignment> finalAssignments,
            String schoolYear) {
        
        return StudentAssignmentResult.builder()
                .schoolYear(schoolYear)
                .totalStudents(phase2Solution.getStudentDemands().size())
                .assignedStudents((int) phase2Solution.getStudentDemands().stream()
                        .filter(d -> d.getAssignedInternship() != null)
                        .count())
                .unassignedStudents((int) phase2Solution.getStudentDemands().stream()
                        .filter(d -> d.getAssignedInternship() == null)
                        .count())
                .score(phase2Solution.getScore().toString())
                .assignments(finalAssignments.stream()
                        .map(assignmentMapper::toDto)
                        .collect(Collectors.toList()))
                .build();
    }
}
