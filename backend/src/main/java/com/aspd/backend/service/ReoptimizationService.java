package com.aspd.backend.service;

import com.aspd.backend.dto.BaselineAssignmentDto;
import com.aspd.backend.dto.BaselineCaptureRequest;
import com.aspd.backend.model.*;
import com.aspd.backend.optimization.OptimizationJobService;
import com.aspd.backend.repository.*;
import com.aspd.backend.solver.InternshipSolution;
import com.aspd.backend.solver.StudentAssignmentSolution;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
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
    private final OptimizationJobService jobService;
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
     * @param internshipBudget Total internship budget (maximum number of internship slots)
     * @param uncompletedInternships Number of uncompleted internships from previous semester
     * @return Optimization solution with assignments
     */
    @Transactional
    public StudentAssignmentSolution reoptimize(String schoolYear, Integer internshipBudget, Integer uncompletedInternships) {
        
        try {
            // Store initial budget
            initialBudget.set(internshipBudget);
            
            log.info("\n========== AUTOMATIC RE-OPTIMIZATION ==========");
            log.info("Target Semester: {}", schoolYear);
            
            // Determine previous semester
            String previousSemester = getPreviousSemester(schoolYear);
            log.info("Previous Semester: {}", previousSemester);
            log.info("==============================================\n");
            
            // SMART PRESERVATION: Check if BOTH student and teacher configs are identical
            log.info("\n========== CONFIG COMPARISON ==========");
            log.info("Comparing {} → {}", previousSemester, schoolYear);
            
            List<StudentConfig> previousStudentConfigs = studentConfigRepository.findByYear(previousSemester);
            List<StudentConfig> currentStudentConfigs = studentConfigRepository.findByYear(schoolYear);
            
            // STEP 3: Load student configurations for target semester
            log.info("\nSTEP 3: Loading student configurations for {}", schoolYear);
            List<StudentConfig> studentConfigs = studentConfigRepository.findByYearWithStudent(schoolYear);
            
            if (studentConfigs.isEmpty()) {
                throw new IllegalStateException(
                    "No student configurations found for school year: " + schoolYear);
            }
            
            log.info("Student configs: {} (previous) vs {} (current)", 
                previousStudentConfigs.size(), currentStudentConfigs.size());
            
            List<Teacher> previousTeachers = teacherRepository.findAllWithConfigs().stream()
                .filter(t -> t.getPlConfigs().stream()
                    .anyMatch(config -> config.getSchoolYear().equals(previousSemester) && config.isActive()))
                .collect(Collectors.toList());
            
            List<Teacher> currentTeachers = teacherRepository.findAllWithConfigs().stream()
                .filter(t -> t.getPlConfigs().stream()
                    .anyMatch(config -> config.getSchoolYear().equals(schoolYear) && config.isActive()))
                .collect(Collectors.toList());
            
            log.info("Teacher configs: {} (previous) vs {} (current)", 
                previousTeachers.size(), currentTeachers.size());
            
            boolean configsFullyIdentical = areAllConfigsIdentical(
                previousStudentConfigs, currentStudentConfigs,
                previousTeachers, currentTeachers,
                previousSemester, schoolYear
            );
            
            log.info("==========================================\n");
            
            if (configsFullyIdentical) {
                log.info("\n⚡⚡⚡ COMPLETE PRESERVATION: Student AND Teacher configs are IDENTICAL ⚡⚡⚡");
                log.info("   Skipping Phase 1 AND Phase 2 - copying complete winter results for 100% preservation");
                
                StudentAssignmentSolution solution = copyCompleteWinterResults(
                    previousSemester, 
                    schoolYear, 
                    currentStudentConfigs
                );
                
                log.info("\n========== RE-OPTIMIZATION COMPLETE (FULL COPY - NO OPTIMIZATION NEEDED) ==========\n");
                return solution;
            }
            
            log.info("\nConfigurations have CHANGED - proceeding with optimization");
            
            // STEP 1: Capture baseline from previous semester
            log.info("\nSTEP 1: Capturing baseline from {}", previousSemester);
            captureBaselineFromPreviousSemester(previousSemester);
            
            // STEP 2: Run Phase 1 for target semester
            log.info("\nSTEP 2: Running Phase 1 for {}", schoolYear);
            runPhase1ForTargetSemester(schoolYear, internshipBudget, uncompletedInternships);
            
            // STEP 3: Run Phase 2 re-optimization with baseline preservation
            log.info("\nSTEP 3: Running Phase 2 re-optimization");
            String semester = getSemesterType(schoolYear);
            StudentAssignmentSolution solution = phase2OptimizationService.optimize(
                currentStudentConfigs,
                schoolYear,
                internshipBudget,
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
     * Re-optimize student assignments asynchronously.
     * This method runs the entire reoptimization workflow in a background thread,
     * including Phase 1 and Phase 2 optimization.
     * 
     * @param schoolYear Academic year with semester notation (e.g., "SoSe2025")
     * @param timeBudget Optional initial time budget
     * @param uncompletedInternships Number of uncompleted internships from previous semester
     * @param phase1TimeLimitSeconds Time limit for Phase 1 solver in seconds
     * @param jobId Job ID for tracking progress
     */
    @Async("optimizationExecutor")
    @Transactional
    public void reoptimizeAsync(
            String schoolYear,
            Integer timeBudget,
            Integer uncompletedInternships,
            Long phase1TimeLimitSeconds,
            String jobId) {
        
        log.info("\n========== ASYNC RE-OPTIMIZATION ==========");
        log.info("Job ID: {}", jobId);
        log.info("Target Semester: {}", schoolYear);
        log.info("Phase 1 Time Limit: {} seconds", phase1TimeLimitSeconds);
        
        try {
            // Mark job as running
            jobService.markJobAsRunning(jobId);
            
            // Store initial budget
            initialBudget.set(timeBudget);
            
            // Determine previous semester
            String previousSemester = getPreviousSemester(schoolYear);
            log.info("Previous Semester: {}", previousSemester);
            log.info("==============================================\n");
            
            // STEP 1: Capture baseline from previous semester
            log.info("STEP 1: Capturing baseline from {}", previousSemester);
            captureBaselineFromPreviousSemester(previousSemester);
            
            // STEP 2: Run Phase 1 for target semester (this calls sync method but we're in async context)
            log.info("\nSTEP 2: Running Phase 1 for {}", schoolYear);
            runPhase1ForTargetSemesterAsync(schoolYear, timeBudget, uncompletedInternships, phase1TimeLimitSeconds);
            
            // STEP 3: Load student configurations for target semester
            log.info("\nSTEP 3: Loading student configurations for {}", schoolYear);
            List<StudentConfig> studentConfigs = studentConfigRepository.findByYearWithStudent(schoolYear);
            
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
            
            log.info("\n========== ASYNC RE-OPTIMIZATION COMPLETE ==========\n");
            
            // Mark job as completed with success message
            long assignedCount = solution.getStudentDemands().stream()
                    .filter(d -> d.getAssignedInternship() != null)
                    .count();
            
            String message = String.format("Reoptimization complete: %d/%d students assigned. Score: %s",
                    assignedCount, solution.getStudentDemands().size(), solution.getScore());
            jobService.markJobAsCompleted(jobId, message);
            
        } catch (Exception e) {
            log.error("Async re-optimization failed for job {}: {}", jobId, e.getMessage(), e);
            jobService.markJobAsFailed(jobId, "Reoptimization failed: " + e.getMessage());
        } finally {
            // Clear ThreadLocal values
            clearBudgetInfo();
        }
    }
    
    /**
     * Run Phase 1 for target semester with configurable time limit (async version).
     */
    private void runPhase1ForTargetSemesterAsync(
            String schoolYear, Integer timeBudget, Integer uncompletedInternships, Long phase1TimeLimitSeconds) {
        
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
        
        // Calculate winter budget used
        double winterBudgetUsedValue = previousInternships.stream()
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
        
        log.info("Winter budget used (PDP_I + ZSP): {}", winterBudgetUsedValue);
        this.winterBudgetUsed.set(winterBudgetUsedValue);
        
        // Calculate final budget
        Integer budget;
        if (timeBudget != null) {
            int uncompleted = (uncompletedInternships != null) ? uncompletedInternships : 0;
            budget = (int) Math.ceil(timeBudget - winterBudgetUsedValue + uncompleted);
            log.info("Budget calculation: {} (initial) - {} (winter used) + {} (uncompleted) = {}",
                timeBudget, winterBudgetUsedValue, uncompleted, budget);
        } else {
            budget = previousInternships.size();
            log.info("Using default budget: {}", budget);
        }
        this.finalBudget.set(budget);
        
        // Load data for Phase 1
        List<StudentConfig> studentConfigs = studentConfigRepository.findByYearWithStudent(schoolYear);
        if (studentConfigs.isEmpty()) {
            throw new IllegalStateException("Keine Studentenkonfigurationen für " + schoolYear + " gefunden");
        }
        
        List<Teacher> teachers = teacherRepository.findAllWithConfigs().stream()
            .filter(Teacher::isActive)
            .collect(Collectors.toList());
        
        List<School> schools = schoolRepository.findAll().stream()
            .filter(s -> Boolean.TRUE.equals(s.getActive()))
            .collect(Collectors.toList());
        
        // Use the blocking optimize method with preservation, but we're already in async context
        // so this won't block the main thread
        InternshipSolution phase1Solution = phase1OptimizationService.optimizeWithPreviousSemester(
            teachers, schools, studentConfigs, schoolYear, budget, previousInternships, phase1TimeLimitSeconds);
        
        log.info("Phase 1 completed for {} with score: {}", schoolYear, phase1Solution.getScore());
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
    private void runPhase1ForTargetSemester(String schoolYear, Integer internshipBudget, Integer uncompletedInternships) {
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
        if (internshipBudget != null) {
            int uncompleted = (uncompletedInternships != null) ? uncompletedInternships : 0;
            budget = (int) Math.ceil(internshipBudget - winterBudgetUsed + uncompleted);
            log.info("Budget calculation: {} (total budget) - {} (winter used) + {} (uncompleted) = {}",
                internshipBudget, winterBudgetUsed, uncompleted, budget);
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
        List<StudentConfig> studentConfigs = studentConfigRepository.findByYearWithStudent(schoolYear);
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
                if (pi.getPraktikumType() == PraktikumType.SFP && pi.getCourse() != null) {
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
     * Check if ALL configurations (students AND teachers) are identical between semesters.
     * 
     * This enables complete preservation:
     * - If both student AND teacher configs are identical, skip Phase 1 and Phase 2 entirely
     * - Copy complete winter results for 100% preservation
     * - Saves computation time and guarantees perfect stability
     * 
     * Student comparison criteria:
     * - Same students (by matriculation number)
     * - Same PDP_II requirements (isPdpII flag)
     * - Same SFP requirements (isSfp flag)
     * - Same main courses for SFP
     * - Same school types
     * 
     * Teacher comparison criteria:
     * - Same teachers (by teacher ID)
     * - Same active status
     * - Same max praktika per year
     * - Same course assignments
     * - Same internship type preferences
     * 
     * @param previousStudentConfigs Student configs from previous semester
     * @param currentStudentConfigs Student configs from current semester
     * @param previousTeachers Teachers from previous semester with configs
     * @param currentTeachers Teachers from current semester with configs
     * @param previousSemester Previous semester year
     * @param currentSemester Current semester year
     * @return true if ALL configs are identical, false otherwise
     */
    private boolean areAllConfigsIdentical(
            List<StudentConfig> previousStudentConfigs,
            List<StudentConfig> currentStudentConfigs,
            List<Teacher> previousTeachers,
            List<Teacher> currentTeachers,
            String previousSemester,
            String currentSemester) {
        
        // ===== PART 1: Compare Student Configurations =====
        
        // IMPORTANT: Only compare students who have PDP_II or SFP requirements
        // (Winter students with only PDP_I/ZSP are not relevant for summer comparison)
        List<StudentConfig> previousRelevantStudents = previousStudentConfigs.stream()
            .filter(c -> c.isPdpII() || c.isSfp())
            .collect(Collectors.toList());
        
        List<StudentConfig> currentRelevantStudents = currentStudentConfigs.stream()
            .filter(c -> c.isPdpII() || c.isSfp())
            .collect(Collectors.toList());
        
        log.info("Relevant students (PDP_II or SFP): {} (previous) vs {} (current)",
            previousRelevantStudents.size(), currentRelevantStudents.size());
        
        if (previousRelevantStudents.size() != currentRelevantStudents.size()) {
            log.info("Config comparison: Different number of relevant students ({} vs {})",
                previousRelevantStudents.size(), currentRelevantStudents.size());
            return false;
        }
        
        Map<Integer, StudentConfig> previousStudentMap = previousRelevantStudents.stream()
            .collect(Collectors.toMap(
                config -> config.getStudent().getMatriculationNbr(),
                config -> config,
                (a, b) -> a
            ));
        
        Map<Integer, StudentConfig> currentStudentMap = currentRelevantStudents.stream()
            .collect(Collectors.toMap(
                config -> config.getStudent().getMatriculationNbr(),
                config -> config,
                (a, b) -> a
            ));
        
        if (!previousStudentMap.keySet().equals(currentStudentMap.keySet())) {
            log.info("Config comparison: Different student sets");
            return false;
        }
        
        // Compare each student's requirements
        for (Integer matriculationNbr : previousStudentMap.keySet()) {
            StudentConfig prev = previousStudentMap.get(matriculationNbr);
            StudentConfig curr = currentStudentMap.get(matriculationNbr);
            
            if (prev.isPdpII() != curr.isPdpII()) {
                log.info("Config comparison: PDP_II flag changed for student {}", matriculationNbr);
                return false;
            }
            
            if (prev.isSfp() != curr.isSfp()) {
                log.info("Config comparison: SFP flag changed for student {}", matriculationNbr);
                return false;
            }
            
            if (curr.isSfp()) {
                Long prevCourseId = prev.getMainCourse() != null ? prev.getMainCourse().getId() : null;
                Long currCourseId = curr.getMainCourse() != null ? curr.getMainCourse().getId() : null;
                
                if (!Objects.equals(prevCourseId, currCourseId)) {
                    log.info("Config comparison: SFP course changed for student {} ({} vs {})",
                        matriculationNbr, prevCourseId, currCourseId);
                    return false;
                }
            }
            
            if (prev.getSchoolType() != curr.getSchoolType()) {
                log.info("Config comparison: School type changed for student {}", matriculationNbr);
                return false;
            }
        }
        
        log.info("✓ Student configs: IDENTICAL");
        
        // ===== PART 2: Compare Teacher Configurations =====
        
        if (previousTeachers.size() != currentTeachers.size()) {
            log.info("Config comparison: Different number of teachers ({} vs {})",
                previousTeachers.size(), currentTeachers.size());
            return false;
        }
        
        // Build maps by teacher ID
        Map<Long, Teacher> previousTeacherMap = previousTeachers.stream()
            .collect(Collectors.toMap(Teacher::getTeacherId, t -> t, (a, b) -> a));
        
        Map<Long, Teacher> currentTeacherMap = currentTeachers.stream()
            .collect(Collectors.toMap(Teacher::getTeacherId, t -> t, (a, b) -> a));
        
        if (!previousTeacherMap.keySet().equals(currentTeacherMap.keySet())) {
            log.info("Config comparison: Different teacher sets");
            return false;
        }
        
        // Compare each teacher's configuration
        for (Long teacherId : previousTeacherMap.keySet()) {
            Teacher prevTeacher = previousTeacherMap.get(teacherId);
            Teacher currTeacher = currentTeacherMap.get(teacherId);
            
            // Get configs for the specific semesters
            TeacherPlConfig prevConfig = prevTeacher.getPlConfigs().stream()
                .filter(c -> c.getSchoolYear().equals(previousSemester) && c.isActive())
                .findFirst()
                .orElse(null);
            
            TeacherPlConfig currConfig = currTeacher.getPlConfigs().stream()
                .filter(c -> c.getSchoolYear().equals(currentSemester) && c.isActive())
                .findFirst()
                .orElse(null);
            
            if (prevConfig == null || currConfig == null) {
                log.info("Config comparison: Missing config for teacher {} (prev={}, curr={})", 
                    teacherId, prevConfig != null, currConfig != null);
                return false;
            }
            
            // Compare max praktika
            if (!Objects.equals(prevConfig.getMaxPraktikaPerYear(), currConfig.getMaxPraktikaPerYear())) {
                log.info("Config comparison: Max praktika changed for teacher {} ({} vs {})", 
                    teacherId, prevConfig.getMaxPraktikaPerYear(), currConfig.getMaxPraktikaPerYear());
                return false;
            }
            
            // Compare course assignments
            Set<Long> prevCourses = prevConfig.getSubjectSpecializations().stream()
                .map(Course::getId)
                .collect(Collectors.toSet());
            Set<Long> currCourses = currConfig.getSubjectSpecializations().stream()
                .map(Course::getId)
                .collect(Collectors.toSet());
            
            if (!prevCourses.equals(currCourses)) {
                log.info("Config comparison: Course assignments changed for teacher {} ({} vs {})", 
                    teacherId, prevCourses, currCourses);
                return false;
            }
            
            // Compare internship preferences
            Set<PraktikumType> prevPrefs = new HashSet<>(prevConfig.getInternshipPreferences());
            Set<PraktikumType> currPrefs = new HashSet<>(currConfig.getInternshipPreferences());
            
            if (!prevPrefs.equals(currPrefs)) {
                log.info("Config comparison: Internship preferences changed for teacher {} ({} vs {})", 
                    teacherId, prevPrefs, currPrefs);
                return false;
            }
        }
        
        log.info("✓ Teacher configs: IDENTICAL");
        log.info("✓✓✓ ALL CONFIGS ARE IDENTICAL - 100% preservation possible");
        return true;
    }
    
    /**
     * Copy complete winter results to summer when ALL configs are identical.
     * 
     * This is used when BOTH student and teacher configs are identical.
     * Ensures 100% preservation by copying:
     * - Phase 1 results (planned internships with teacher assignments)
     * - Phase 2 results (student assignments)
     * 
     * Steps:
     * 1. Copy all planned internships from winter to summer (Phase 1 results)
     * 2. Copy all student assignments from winter to summer (Phase 2 results)
     * 3. Update all school years to current semester
     * 4. Save everything to database
     * 5. Return as solution
     * 
     * @param previousSemester Previous semester year (e.g., "WiSe2025")
     * @param currentSemester Current semester year (e.g., "SoSe2025")
     * @param studentConfigs Current semester student configs
     * @return Solution with copied assignments
     */
    private StudentAssignmentSolution copyCompleteWinterResults(
            String previousSemester,
            String currentSemester,
            List<StudentConfig> studentConfigs) {
        
        log.info("Copying COMPLETE winter results from {} to {}", previousSemester, currentSemester);
        
        // STEP 1: Copy Phase 1 results (planned internships)
        // Only copy PDP_II and SFP internships (not PDP_I or ZSP which are winter-only)
        List<PlannedInternship> winterInternships = plannedInternshipRepository
            .findBySchoolYear(previousSemester);
        
        if (winterInternships.isEmpty()) {
            throw new IllegalStateException(
                "No planned internships found for " + previousSemester
            );
        }
        
        // Filter to only PDP_II and SFP internships for summer
        List<PlannedInternship> relevantWinterInternships = winterInternships.stream()
            .filter(pi -> pi.getPraktikumType() == PraktikumType.PDP_II || 
                         pi.getPraktikumType() == PraktikumType.SFP)
            .collect(Collectors.toList());
        
        log.info("Filtering winter internships: {} total → {} relevant (PDP_II + SFP only)",
            winterInternships.size(), relevantWinterInternships.size());
        
        // Create copies for summer semester
        List<PlannedInternship> summerInternships = relevantWinterInternships.stream()
            .map(winter -> PlannedInternship.builder()
                .praktikumType(winter.getPraktikumType())
                .schoolType(winter.getSchoolType())
                .course(winter.getCourse())
                .originalCourse(winter.getOriginalCourse())
                .schoolYear(currentSemester)  // Update to summer
                .maxCapacity(winter.getMaxCapacity())
                .currentAssignments(0)  // Will be updated when copying student assignments
                .active(winter.isActive())
                .assignedTeacher(winter.getAssignedTeacher())
                .assignedSchool(winter.getAssignedSchool())
                .build())
            .collect(Collectors.toList());
        
        // Save Phase 1 results
        List<PlannedInternship> savedInternships = plannedInternshipRepository.saveAll(summerInternships);
        log.info("Copied {} planned internships (Phase 1 results)", savedInternships.size());
        
        // STEP 2: Copy Phase 2 results (student assignments)
        List<StudentInternshipDemand> previousDemands = demandRepository
            .findBySchoolYear(previousSemester);
        
        // Create new demands for current semester by copying previous assignments
        List<StudentInternshipDemand> newDemands = new ArrayList<>();
        
        for (StudentConfig config : studentConfigs) {
            int matriculationNbr = config.getStudent().getMatriculationNbr();
            
            // Find previous demands for this student (only PDP_II and SFP for summer)
            List<StudentInternshipDemand> prevStudentDemands = previousDemands.stream()
                .filter(d -> d.getStudentConfig() != null)
                .filter(d -> d.getStudentConfig().getStudent()
                    .getMatriculationNbr() == matriculationNbr)
                .filter(d -> d.getPraktikumType() == PraktikumType.PDP_II || 
                            d.getPraktikumType() == PraktikumType.SFP)
                .collect(Collectors.toList());
            
            // Copy each demand to new semester
            for (StudentInternshipDemand prevDemand : prevStudentDemands) {
                // Find matching summer internship (exact match by teacher and properties)
                PlannedInternship matchingInternship = savedInternships.stream()
                    .filter(pi -> pi.getPraktikumType() == prevDemand.getPraktikumType())
                    .filter(pi -> pi.getSchoolType() == prevDemand.getStudentConfig().getSchoolType())
                    .filter(pi -> {
                        // For SFP, also match course
                        if (prevDemand.getPraktikumType() == PraktikumType.SFP) {
                            return Objects.equals(
                                pi.getCourse() != null ? pi.getCourse().getId() : null,
                                prevDemand.getAssignedInternship() != null && 
                                prevDemand.getAssignedInternship().getCourse() != null ?
                                prevDemand.getAssignedInternship().getCourse().getId() : null
                            );
                        }
                        return true;
                    })
                    .filter(pi -> pi.getAssignedTeacher() != null)
                    .filter(pi -> Objects.equals(
                        pi.getAssignedTeacher().getTeacherId(),
                        prevDemand.getAssignedInternship() != null &&
                        prevDemand.getAssignedInternship().getAssignedTeacher() != null ?
                        prevDemand.getAssignedInternship().getAssignedTeacher().getTeacherId() : null
                    ))
                    .findFirst()
                    .orElse(null);
                
                if (matchingInternship != null) {
                    // Create new demand copying the previous assignment
                    StudentInternshipDemand newDemand = StudentInternshipDemand.builder()
                        .studentConfig(config)
                        .praktikumType(prevDemand.getPraktikumType())
                        .schoolYear(currentSemester)
                        .assignedInternship(matchingInternship)
                        .build();
                    
                    newDemands.add(newDemand);
                    
                    // Update internship capacity
                    matchingInternship.setCurrentAssignments(
                        matchingInternship.getCurrentAssignments() + 1
                    );
                } else {
                    log.warn("Could not find matching internship for student {} - {} {}",
                        matriculationNbr,
                        prevDemand.getPraktikumType(),
                        prevDemand.getStudentConfig().getSchoolType());
                }
            }
        }
        
        // Save new demands
        List<StudentInternshipDemand> savedDemands = demandRepository.saveAll(newDemands);
        log.info("Copied {} student demands (Phase 2 results)", savedDemands.size());
        
        // Save updated internship capacities
        plannedInternshipRepository.saveAll(savedInternships);
        
        // STEP 3: Create InternshipAssignment records (needed for frontend display)
        log.info("Creating internship assignment records for summer...");
        List<InternshipAssignment> assignments = phase2OptimizationService
            .createFinalAssignments(savedDemands, currentSemester);
        
        // Delete existing assignments first to avoid duplicates
        List<InternshipAssignment> existingAssignments = assignmentRepository.findBySchoolYear(currentSemester);
        if (!existingAssignments.isEmpty()) {
            log.info("Deleting {} existing assignments for {}", existingAssignments.size(), currentSemester);
            assignmentRepository.deleteAll(existingAssignments);
        }
        
        // Save new assignments
        List<InternshipAssignment> savedAssignments = assignmentRepository.saveAll(assignments);
        log.info("Created {} internship assignments for frontend", savedAssignments.size());
        
        log.info("✓ Complete winter results copied: {} internships + {} demands + {} assignments",
            savedInternships.size(), savedDemands.size(), savedAssignments.size());
        
        // Build solution object (mimics Phase 2 output)
        StudentAssignmentSolution solution = new StudentAssignmentSolution();
        solution.setStudentDemands(savedDemands);
        solution.setScore(org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore.ZERO);
        
        return solution;
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
     * - "SoSe26" -> "WiSe25-26" (summer 2026 uses winter 2025-26 baseline)
     * - "WiSe25-26" -> "SoSe25" (winter 2025-26 uses summer 2025 baseline)
     * 
     * @param schoolYear Current semester (e.g., "SoSe26" or "WiSe25-26")
     * @return Previous semester (e.g., "WiSe25-26" or "SoSe25")
     */
    private String getPreviousSemester(String schoolYear) {
        if (schoolYear.startsWith("SoSe")) {
            // Summer semester (SoSe26) -> use winter from previous academic year (WiSe25-26)
            String year = schoolYear.substring(4);
            int yearNum = Integer.parseInt(year);
            int prevYear = yearNum - 1;
            return String.format("WiSe%02d-%02d", prevYear, yearNum);
        } else if (schoolYear.startsWith("WiSe")) {
            // Winter semester (WiSe25-26) -> use previous year summer semester (SoSe25)
            // Extract the first year from the format WiSe25-26
            String yearPart = schoolYear.substring(4);
            String firstYear = yearPart.split("-")[0];
            return "SoSe" + firstYear;
        }
        throw new IllegalArgumentException("Invalid semester format: " + schoolYear + 
            ". Expected format: 'WiSe25-26' or 'SoSe26'");
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
