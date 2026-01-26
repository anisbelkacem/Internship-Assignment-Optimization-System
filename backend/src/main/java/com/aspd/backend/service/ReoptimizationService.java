package com.aspd.backend.service;

import com.aspd.backend.dto.BaselineAssignmentDto;
import com.aspd.backend.dto.BaselineCaptureRequest;
import com.aspd.backend.model.*;
import com.aspd.backend.repository.*;
import com.aspd.backend.solver.InternshipSolution;
import com.aspd.backend.solver.StudentAssignmentSolution;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for orchestrating re-optimization workflow.
 * Handles the complete process of re-optimizing for a new semester using
 * previous semester's results as baseline.
 * 
 * This service now automatically handles all prerequisites:
 * 1. Captures baseline from previous semester if not exists
 * 2. Runs Phase 1 for target semester if not exists
 * 3. Runs Phase 2 re-optimization with baseline preservation
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReoptimizationService {

    private final Phase1OptimizationService phase1OptimizationService;
    private final Phase2OptimizationService phase2OptimizationService;
    private final BaselineService baselineService;
    private final StudentConfigRepository studentConfigRepository;
    private final TeacherRepository teacherRepository;
    private final SchoolRepository schoolRepository;
    private final PlannedInternshipRepository plannedInternshipRepository;
    private final BaselineAssignmentRepository baselineAssignmentRepository;
    private final StudentInternshipDemandRepository demandRepository;
    private final InternshipAssignmentRepository assignmentRepository;
    
    // Thread-safe storage for budget information during reoptimization
    private final ThreadLocal<Double> winterBudgetUsed = new ThreadLocal<>();
    private final ThreadLocal<Integer> initialBudget = new ThreadLocal<>();
    private final ThreadLocal<Integer> finalBudget = new ThreadLocal<>();

    /**
     * Re-optimize student assignments for a new semester using previous semester as baseline.
     * 
     * This method automatically handles all prerequisites:
     * 1. Captures baseline from previous semester (deletes old, creates fresh)
     * 2. Runs Phase 1 for target semester (deletes old, creates fresh)
     * 3. Runs Phase 2 re-optimization with baseline preservation
     * 4. Saves results as new baseline
     * 
     * @param schoolYear Academic year with semester notation (e.g., "SoSe2025")
     * @param timeBudget Optional initial time budget
     * @param uncompletedInternships Number of uncompleted internships from previous semester
     * @return Optimization solution with assignments
     */
    @Transactional
    public StudentAssignmentSolution reoptimize(String schoolYear, Integer timeBudget, Integer uncompletedInternships) {
        
        try {
            // Store initial budget
            initialBudget.set(timeBudget);
            
            log.info("\n========== AUTOMATIC RE-OPTIMIZATION ==========");
            log.info("Target Semester: {}", schoolYear);
            
            // Determine previous semester
            String previousSemester = getPreviousSemester(schoolYear);
            log.info("Previous Semester: {}", previousSemester);
            log.info("==============================================\n");
            
            // STEP 1: Capture baseline from previous semester
            log.info("STEP 1: Capturing baseline from {}", previousSemester);
            captureBaselineFromPreviousSemester(previousSemester);
            
            // STEP 2: Run Phase 1 for target semester
            log.info("\nSTEP 2: Running Phase 1 for {}", schoolYear);
            runPhase1ForTargetSemester(schoolYear, timeBudget, uncompletedInternships);
            
            // STEP 3: Load student configurations for target semester
            log.info("\nSTEP 3: Loading student configurations for {}", schoolYear);
            List<StudentConfig> studentConfigs = studentConfigRepository.findByYear(schoolYear);
            
            if (studentConfigs.isEmpty()) {
                throw new IllegalStateException(
                    "No student configurations found for school year: " + schoolYear);
            }
            
            log.info("Loaded {} student configurations", studentConfigs.size());
            
            // STEP 4: Run Phase 2 re-optimization with baseline preservation
            log.info("\nSTEP 4: Running Phase 2 re-optimization");
            String semester = getSemesterType(schoolYear);
            StudentAssignmentSolution solution = phase2OptimizationService.optimize(
                studentConfigs,
                schoolYear,
                timeBudget,
                semester
            );
            
            log.info("Re-optimization completed with score: {}", solution.getScore());
            
            // STEP 5: Save results as baseline for this semester
            log.info("\nSTEP 5: Saving results as baseline for {}", schoolYear);
            saveAsBaseline(solution, schoolYear);
            
            log.info("\n========== RE-OPTIMIZATION COMPLETE ==========\n");
            
            return solution;
        } finally {
            // Note: Don't clear ThreadLocal here, controller needs to access values
            // Controller will clear after building response
        }
    }
    
    /**
     * Capture baseline from previous semester.
     * Deletes existing baseline and creates fresh from current assignments.
     */
    private void captureBaselineFromPreviousSemester(String previousSemester) {
        // Delete old baseline if exists
        if (baselineAssignmentRepository.existsBySchoolYear(previousSemester)) {
            log.info("Deleting existing baseline for {}", previousSemester);
            baselineAssignmentRepository.deleteBySchoolYear(previousSemester);
        }
        
        // Get all demands with assignments from previous semester
        List<StudentInternshipDemand> demands = demandRepository.findBySchoolYear(previousSemester);
        List<StudentInternshipDemand> assignedDemands = demands.stream()
            .filter(d -> d.getAssignedInternship() != null)
            .collect(Collectors.toList());
        
        if (assignedDemands.isEmpty()) {
            log.warn("No assignments found for {}. Baseline will be empty.", previousSemester);
            return;
        }
        
        // Capture baseline
        List<Long> demandIds = assignedDemands.stream()
            .map(StudentInternshipDemand::getId)
            .collect(Collectors.toList());
        
        BaselineCaptureRequest request = BaselineCaptureRequest.builder()
            .schoolYear(previousSemester)
            .studentDemandIds(demandIds)
            .overwriteExisting(true)
            .notes("Auto-captured for re-optimization")
            .build();
        
        List<BaselineAssignmentDto> baseline = baselineService.captureBaseline(request);
        log.info("Captured {} baseline assignments from {}", baseline.size(), previousSemester);
    }
    
    /**
     * Run Phase 1 for target semester.
     * NEW APPROACH: Copy winter PDP_II and SFP assignments as fixed, then optimize only for additional demand.
     * This ensures teacher assignments are preserved from winter to summer.
     */
    private void runPhase1ForTargetSemester(String schoolYear, Integer timeBudget, Integer uncompletedInternships) {
        // Delete old baseline assignments first (foreign key constraint)
        if (baselineAssignmentRepository.existsBySchoolYear(schoolYear)) {
            log.info("Deleting existing baseline assignments for {}", schoolYear);
            baselineAssignmentRepository.deleteBySchoolYear(schoolYear);
        }
        
        // Delete old student internship demands first (foreign key to planned_internships)
        List<StudentInternshipDemand> existingDemands = demandRepository.findBySchoolYear(schoolYear);
        if (!existingDemands.isEmpty()) {
            log.info("Deleting {} existing student internship demands for {}", existingDemands.size(), schoolYear);
            demandRepository.deleteAll(existingDemands);
        }
        
        // Delete old internship assignments (foreign key to planned_internships)
        List<InternshipAssignment> existingAssignments = assignmentRepository.findBySchoolYear(schoolYear);
        if (!existingAssignments.isEmpty()) {
            log.info("Deleting {} existing internship assignments for {}", existingAssignments.size(), schoolYear);
            assignmentRepository.deleteAll(existingAssignments);
        }
        
        // Delete old Phase 1 results if exist
        List<PlannedInternship> existing = plannedInternshipRepository.findBySchoolYear(schoolYear);
        if (!existing.isEmpty()) {
            log.info("Deleting {} existing planned internships for {}", existing.size(), schoolYear);
            plannedInternshipRepository.deleteAll(existing);
        }
        
        // Get previous semester data
        String previousSemester = getPreviousSemester(schoolYear);
        List<PlannedInternship> previousInternships = plannedInternshipRepository.findBySchoolYear(previousSemester);
        
        if (previousInternships.isEmpty()) {
            throw new IllegalStateException("Lehrerzuweisungen (Phase 1) fehlt für " + previousSemester + 
                ". Bitte führen Sie zuerst Phase 1 Optimierung für " + previousSemester + " aus.");
        }
        
        // Calculate winter budget used (only PDP_I and ZSP)
        double winterBudgetUsed = previousInternships.stream()
            .filter(pi -> pi.getAssignedTeacher() != null)
            .filter(pi -> "PDP_I".equals(pi.getPraktikumType().name()) || "ZSP".equals(pi.getPraktikumType().name()))
            .collect(Collectors.groupingBy(
                pi -> pi.getAssignedTeacher().getTeacherId(),
                Collectors.mapping(pi -> pi.getPraktikumType().name(), Collectors.toSet())
            ))
            .values()
            .stream()
            .mapToDouble(types -> types.size() * 0.5)
            .sum();
        
        log.info("Winter budget used (PDP_I + ZSP): {}", winterBudgetUsed);
        this.winterBudgetUsed.set(winterBudgetUsed);
        
        // Calculate final budget
        Integer budget;
        if (timeBudget != null) {
            int uncompleted = (uncompletedInternships != null) ? uncompletedInternships : 0;
            budget = (int) Math.ceil(timeBudget - winterBudgetUsed + uncompleted);
            log.info("Budget calculation: {} (initial) - {} (winter used) + {} (uncompleted) = {}",
                timeBudget, winterBudgetUsed, uncompleted, budget);
        } else {
            budget = previousInternships.size();
            log.info("Using default budget: {}", budget);
        }
        this.finalBudget.set(budget);
        
        // STEP 1: Load summer teacher configs to determine available teachers
        List<Teacher> summerTeachers = teacherRepository.findAllWithConfigs().stream()
            .filter(Teacher::isActive)
            .filter(t -> t.getPlConfigs().stream()
                .anyMatch(config -> config.getSchoolYear().equals(schoolYear) && config.isActive()))
            .collect(Collectors.toList());
        
        Set<Long> summerTeacherIds = summerTeachers.stream()
            .map(Teacher::getTeacherId)
            .collect(Collectors.toSet());
        
        log.info("Found {} teachers configured for {}", summerTeacherIds.size(), schoolYear);
        
        // STEP 2: Copy winter PDP_II and SFP assignments as FIXED (only if teacher is available in summer)
        List<PlannedInternship> fixedInternships = new ArrayList<>();
        List<PlannedInternship> needReassignment = new ArrayList<>();
        
        previousInternships.stream()
            .filter(pi -> pi.getPraktikumType() == PraktikumType.PDP_II || 
                         pi.getPraktikumType() == PraktikumType.SFP)
            .forEach(pi -> {
                boolean teacherAvailable = pi.getAssignedTeacher() != null && 
                    summerTeacherIds.contains(pi.getAssignedTeacher().getTeacherId());
                
                if (teacherAvailable) {
                    // Teacher is available - keep as fixed
                    fixedInternships.add(PlannedInternship.builder()
                        .praktikumType(pi.getPraktikumType())
                        .schoolType(pi.getSchoolType())
                        .course(pi.getCourse())
                        .originalCourse(pi.getOriginalCourse())
                        .schoolYear(schoolYear)
                        .maxCapacity(pi.getMaxCapacity())
                        .currentAssignments(0)
                        .active(true)
                        .assignedTeacher(pi.getAssignedTeacher())
                        .assignedSchool(pi.getAssignedSchool())
                        .build());
                } else {
                    // Teacher not available - needs reassignment
                    needReassignment.add(PlannedInternship.builder()
                        .praktikumType(pi.getPraktikumType())
                        .schoolType(pi.getSchoolType())
                        .course(pi.getCourse())
                        .originalCourse(pi.getOriginalCourse())
                        .schoolYear(schoolYear)
                        .maxCapacity(pi.getMaxCapacity())
                        .currentAssignments(0)
                        .active(false)  // Will be optimized
                        .assignedTeacher(null)  // Will be reassigned
                        .build());
                }
            });
        
        log.info("Copied {} FIXED internships (teachers available in summer)", fixedInternships.size());
        log.info("Marked {} internships for reassignment (teachers unavailable)", needReassignment.size());
        
        // STEP 3: Load summer student configs to determine additional demand
        List<StudentConfig> studentConfigs = studentConfigRepository.findByYear(schoolYear);
        if (studentConfigs.isEmpty()) {
            throw new IllegalStateException("No student configurations for " + schoolYear);
        }
        
        // Count summer demand by type and school type
        Map<String, Long> summerDemand = studentConfigs.stream()
            .flatMap(config -> {
                List<String> demands = new ArrayList<>();
                if (config.isPdpII()) demands.add("PDP_II/" + config.getSchoolType());
                if (config.isSfp()) demands.add("SFP/" + config.getSchoolType() + "/" + config.getMainCourse().getId());
                return demands.stream();
            })
            .collect(Collectors.groupingBy(k -> k, Collectors.counting()));
        
        // Count fixed supply
        Map<String, Long> fixedSupply = fixedInternships.stream()
            .map(pi -> {
                if (pi.getPraktikumType() == PraktikumType.SFP) {
                    return pi.getPraktikumType() + "/" + pi.getSchoolType() + "/" + pi.getCourse().getId();
                }
                return pi.getPraktikumType() + "/" + pi.getSchoolType();
            })
            .collect(Collectors.groupingBy(k -> k, Collectors.counting()));
        
        // STEP 3: Create NEW slots for additional demand (these will be optimized)
        List<PlannedInternship> newSlots = new ArrayList<>();
        for (Map.Entry<String, Long> entry : summerDemand.entrySet()) {
            String key = entry.getKey();
            long demand = entry.getValue();
            long supply = fixedSupply.getOrDefault(key, 0L);
            long additionalNeeded = Math.max(0, demand - supply);
            
            if (additionalNeeded > 0) {
                String[] parts = key.split("/");
                PraktikumType type = PraktikumType.valueOf(parts[0]);
                SchoolType schoolType = SchoolType.valueOf(parts[1]);
                Course course = null;
                if (type == PraktikumType.SFP && parts.length > 2) {
                    Long courseId = Long.parseLong(parts[2]);
                    course = studentConfigs.stream()
                        .map(StudentConfig::getMainCourse)
                        .filter(c -> c != null && c.getId().equals(courseId))
                        .findFirst()
                        .orElse(null);
                }
                
                for (int i = 0; i < additionalNeeded; i++) {
                    newSlots.add(PlannedInternship.builder()
                        .praktikumType(type)
                        .schoolType(schoolType)
                        .course(course)
                        .originalCourse(type == PraktikumType.SFP ? course : null)
                        .schoolYear(schoolYear)
                        .maxCapacity(type == PraktikumType.PDP_II ? 2 : 4)
                        .currentAssignments(0)
                        .active(false)  // Will be decided by optimizer
                        .assignedTeacher(null)  // Will be assigned by optimizer
                        .build());
                }
            }
        }
        
        log.info("Created {} NEW slots for additional demand", newSlots.size());
        
        // STEP 4: Combine fixed, reassignment, and new slots, then run optimization
        List<PlannedInternship> allInternships = new ArrayList<>();
        allInternships.addAll(fixedInternships);
        allInternships.addAll(needReassignment);  // Include slots needing reassignment
        allInternships.addAll(newSlots);
        
        // Load resources - use only summer-configured teachers
        List<School> schools = schoolRepository.findByActiveTrue();
        
        // Run optimization with mixed fixed/optimizable internships
        InternshipSolution solution = phase1OptimizationService.optimizeWithFixedInternships(
            summerTeachers,  // Only summer-configured teachers
            schools,
            allInternships,
            studentConfigs,
            schoolYear,
            budget
        );
        
        log.info("Phase 1 completed: {} total internships ({} fixed + {} reassigned + {} new)", 
            solution.getPlannedInternships().size(), fixedInternships.size(), 
            needReassignment.size(), newSlots.size());
    }
    
    /**
     * Calculate how many teacher assignments were preserved from winter to summer.
     * Compares winter PDP_II and SFP assignments with summer results.
     * 
     * Match criteria:
     * - Same PraktikumType
     * - Same SchoolType
     * - Same Teacher
     * - For SFP: Also same Course
     * 
     * @param winterInternships Winter semester internships (previous)
     * @param summerInternships Summer semester internships (current)
     * @return Number of preserved teacher assignments
     */
    private int calculateTeacherAssignmentsPreserved(
            List<PlannedInternship> winterInternships,
            List<PlannedInternship> summerInternships) {
        
        // Filter winter PDP_II and SFP assignments with teachers
        Map<String, Teacher> winterAssignments = winterInternships.stream()
            .filter(pi -> pi.isActive() && pi.getAssignedTeacher() != null)
            .filter(pi -> pi.getPraktikumType() == PraktikumType.PDP_II || 
                         pi.getPraktikumType() == PraktikumType.SFP)
            .collect(Collectors.toMap(
                pi -> buildAssignmentKey(pi),
                PlannedInternship::getAssignedTeacher,
                (existing, replacement) -> existing // Keep first if duplicate keys
            ));
        
        // Count how many summer assignments match winter
        int preserved = 0;
        for (PlannedInternship summer : summerInternships) {
            if (!summer.isActive() || summer.getAssignedTeacher() == null) {
                continue;
            }
            
            String key = buildAssignmentKey(summer);
            Teacher winterTeacher = winterAssignments.get(key);
            
            if (winterTeacher != null && winterTeacher.equals(summer.getAssignedTeacher())) {
                preserved++;
            }
        }
        
        return preserved;
    }
    
    /**
     * Build unique key for teacher assignment comparison.
     * For PDP_II: "PDP_II/GS"
     * For SFP: "SFP/GS/courseId"
     */
    private String buildAssignmentKey(PlannedInternship pi) {
        if (pi.getPraktikumType() == PraktikumType.SFP && pi.getCourse() != null) {
            return pi.getPraktikumType() + "/" + pi.getSchoolType() + "/" + pi.getCourse().getId();
        }
        return pi.getPraktikumType() + "/" + pi.getSchoolType();
    }

    /**
     * Determine the previous semester from current semester notation.
     * Examples:
     * - "SoSe2025" -> "WiSe2025" (summer 2025 uses winter 2025 baseline)
     * - "WiSe2026" -> "SoSe2025" (winter 2026 uses summer 2025 baseline)
     * 
     * @param schoolYear Current semester (e.g., "SoSe2025")
     * @return Previous semester (e.g., "WiSe2025")
     */
    private String getPreviousSemester(String schoolYear) {
        if (schoolYear.startsWith("SoSe")) {
            // Summer semester -> use same year winter semester
            String year = schoolYear.substring(4);
            return "WiSe" + year;
        } else if (schoolYear.startsWith("WiSe")) {
            // Winter semester -> use previous year summer semester
            String year = schoolYear.substring(4);
            int yearNum = Integer.parseInt(year);
            return "SoSe" + (yearNum - 1);
        }
        throw new IllegalArgumentException("Invalid semester format: " + schoolYear + 
            ". Expected format: 'WiSe2025' or 'SoSe2025'");
    }

    /**
     * Extract semester type from school year notation.
     * 
     * @param schoolYear School year (e.g., "WiSe2025", "SoSe2025")
     * @return Semester type ("winter" or "summer")
     */
    private String getSemesterType(String schoolYear) {
        if (schoolYear.startsWith("WiSe")) {
            return "winter";
        } else if (schoolYear.startsWith("SoSe")) {
            return "summer";
        }
        throw new IllegalArgumentException("Invalid semester format: " + schoolYear);
    }

    /**
     * Save optimization results as baseline for future re-optimizations.
     * 
     * @param solution The optimization solution
     * @param schoolYear School year
     */
    private void saveAsBaseline(StudentAssignmentSolution solution, String schoolYear) {
        
        log.info("Saving re-optimization results as baseline for year={}", schoolYear);
        
        // Create final assignments
        List<InternshipAssignment> assignments = phase2OptimizationService
            .createFinalAssignments(solution.getStudentDemands(), schoolYear);
        
        log.info("Created {} final assignments", assignments.size());
        
        // Delete existing baseline assignments first (foreign key constraint)
        if (baselineAssignmentRepository.existsBySchoolYear(schoolYear)) {
            log.info("Deleting existing baseline assignments for {}", schoolYear);
            baselineAssignmentRepository.deleteBySchoolYear(schoolYear);
        }
        
        // Delete existing internship assignments for this school year to avoid duplicates
        List<InternshipAssignment> existingAssignments = assignmentRepository.findBySchoolYear(schoolYear);
        if (!existingAssignments.isEmpty()) {
            log.info("Deleting {} existing assignments for year {}", existingAssignments.size(), schoolYear);
            assignmentRepository.deleteAll(existingAssignments);
        }
        
        // Save the new assignments to database
        List<InternshipAssignment> savedAssignments = assignmentRepository.saveAll(assignments);
        log.info("Saved {} student assignments to database", savedAssignments.size());
        
        // Capture baseline (overwrite if exists since this is the new optimization)
        // Extract demand IDs from assignments
        List<Long> demandIds = assignments.stream()
            .map(a -> a.getStudentConfig().getId())
            .distinct()
            .collect(java.util.stream.Collectors.toList());
        
        BaselineCaptureRequest request = BaselineCaptureRequest.builder()
            .schoolYear(schoolYear)
            .studentDemandIds(demandIds)
            .overwriteExisting(true)
            .notes("Auto-saved from re-optimization")
            .build();
        
        List<BaselineAssignmentDto> baseline = baselineService.captureBaseline(request);
        
        log.info("Saved {} baseline assignments for future re-optimizations", baseline.size());
    }
    
    /**
     * Get the winter budget used during the last reoptimization.
     * This value represents the budget consumed in winter (PDP_I + ZSP).
     */
    public Double getWinterBudgetUsed() {
        return winterBudgetUsed.get();
    }
    
    /**
     * Get the initial budget provided for the last reoptimization.
     */
    public Integer getInitialBudget() {
        return initialBudget.get();
    }
    
    /**
     * Get the final calculated budget for the last reoptimization.
     */
    public Integer getFinalBudget() {
        return finalBudget.get();
    }
    
    /**
     * Clear the thread-local budget values after use.
     * Should be called by controller after building response.
     */
    public void clearBudgetInfo() {
        winterBudgetUsed.remove();
        initialBudget.remove();
        finalBudget.remove();
    }
}
