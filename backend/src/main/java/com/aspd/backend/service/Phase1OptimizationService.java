package com.aspd.backend.service;

import com.aspd.backend.model.*;
import com.aspd.backend.optimization.OptimizationJobService;
import com.aspd.backend.repository.CourseRepository;
import com.aspd.backend.repository.PlannedInternshipRepository;
import com.aspd.backend.solver.InternshipEasyScoreCalculator;
import com.aspd.backend.solver.InternshipSolution;
import com.aspd.backend.dto.CoordinatesDto;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.api.solver.SolverFactory;
import org.optaplanner.core.api.solver.SolverJob;
import org.optaplanner.core.api.solver.SolverManager;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.aspd.backend.common.constants.InternshipConstants.*;

/**
 * Service for Phase 1 optimization: Assign teachers and schools to planned internships.
 */
@Service
@Slf4j
public class Phase1OptimizationService {

    private final PlannedInternshipRepository plannedInternshipRepository;
    private final CourseRepository courseRepository;
    private final InternshipEasyScoreCalculator scoreCalculator;
    private final SolverManager<InternshipSolution, String> solverManager;
    private final OptimizationJobService jobService;

    public Phase1OptimizationService(
            PlannedInternshipRepository plannedInternshipRepository,
            CourseRepository courseRepository,
            InternshipEasyScoreCalculator scoreCalculator,
            @Qualifier("phase1SolverManager") SolverManager<InternshipSolution, String> solverManager,
            OptimizationJobService jobService) {
        this.plannedInternshipRepository = plannedInternshipRepository;
        this.courseRepository = courseRepository;
        this.scoreCalculator = scoreCalculator;
        this.solverManager = solverManager;
        this.jobService = jobService;
    }

    /**
     * Run Phase 1: Teacher and School assignment.
     * 
     * This phase:
     * 1. Creates upper bounds for internship slots (max needed for each type)
     * 2. Lets the solver decide which slots to activate
     * 3. Assigns teachers and schools to active slots
     * 
     * @param teachers Available teachers with their configurations
     * @param schools Available schools
     * @param studentConfigs Student configurations (to determine demand)
     * @param schoolYear Academic year (e.g., "2024/2025")
     * @param timeBudget Total internship slots budget (e.g., 50 slots max)
     * @return Phase 1 solution with active internship assignments
     */
    @Transactional
    public InternshipSolution optimize(
            List<Teacher> teachers,
            List<School> schools,
            List<StudentConfig> studentConfigs,
            String schoolYear,
            Integer timeBudget) {
        
        log.info("\n========== PHASE 1: Teacher & School Assignment ==========");
        log.info("Input: {} teachers, {} schools, {} students", 
                 teachers.size(), schools.size(), studentConfigs.size());
        log.info("[BUDGET CHECK] Received timeBudget parameter: {}", timeBudget);
        log.info("Total internship slots budget: {}\n", timeBudget);
        
        // Create internship slots (one per student per checked type)
        List<PlannedInternship> plannedInternships = createPlannedInternshipsFromDemand(
                studentConfigs, schoolYear);
        
        // Build ZSP course distribution maps from student preferences
        ZspCourseDistribution zspDistribution = buildZspCourseDistribution(studentConfigs);

        // Build PDP student coordinate lists (GS/MS) from semester address, falling back to home address
        List<CoordinatesDto> pdpGsStudentCoords = buildPdpStudentCoords(studentConfigs, SchoolType.GS);
        List<CoordinatesDto> pdpMsStudentCoords = buildPdpStudentCoords(studentConfigs, SchoolType.MS);
        
        log.info("Created {} internship slots\n", plannedInternships.size());
        
        // Fetch all active courses for ZSP assignment
        List<Course> courses = courseRepository.findByActiveTrue();
        
        // Run Phase 1
        InternshipSolution phase1Result = runPhase1(
            teachers, schools, courses, plannedInternships, schoolYear, timeBudget, zspDistribution,
            pdpGsStudentCoords, pdpMsStudentCoords);
        
        // Remove inactive internships (solver decided we don't need them)
        List<PlannedInternship> activeInternships = phase1Result.getPlannedInternships().stream()
                .filter(PlannedInternship::isActive)
                .toList();
        
        // Deduplicate based on type, schoolType, course, teacher, and school
        Map<String, PlannedInternship> uniqueInternships = new LinkedHashMap<>();
        for (PlannedInternship pi : activeInternships) {
            String key = buildInternshipKey(pi);
            if (!uniqueInternships.containsKey(key)) {
                uniqueInternships.put(key, pi);
            } else {
                log.debug("Skipping duplicate internship: {}", key);
            }
        }
        
        List<PlannedInternship> deduplicatedInternships = new ArrayList<>(uniqueInternships.values());
        log.info("Deduplicated internships: {} active -> {} unique", 
                 activeInternships.size(), deduplicatedInternships.size());
        
        // Populate schools from assigned teachers (post-optimization step)
        deduplicatedInternships.forEach(PlannedInternship::populateSchoolFromTeacher);
        
        // Save only unique active internships to database
        List<PlannedInternship> savedInternships = plannedInternshipRepository.saveAll(deduplicatedInternships);
        
        phase1Result.setPlannedInternships(savedInternships);
        
        long activeCount = savedInternships.stream()
                .filter(PlannedInternship::isActive)
                .count();
        
        log.info("========== PHASE 1 COMPLETE ==========");
        log.info("Active internships: {}/{}", activeCount, savedInternships.size());
        log.info("Score: {}\n", phase1Result.getScore());
        
        return phase1Result;
    }
    
    /**
     * Run Phase 1 optimization with previous semester data for teacher preservation.
     * Used during reoptimization (e.g., WiSe → SoSe) to prefer keeping the same teachers.
     * 
     * @param teachers Available teachers with their configurations
     * @param schools Available schools
     * @param studentConfigs Student configurations (to determine demand)
     * @param schoolYear Academic year (e.g., "SoSe2025")
     * @param timeBudget Total internship slots budget
     * @param previousSemesterInternships Previous semester's teacher assignments for preservation
     * @return Phase 1 solution with active internship assignments
     */
    @Transactional
    public InternshipSolution optimizeWithPreviousSemester(
            List<Teacher> teachers,
            List<School> schools,
            List<StudentConfig> studentConfigs,
            String schoolYear,
            Integer timeBudget,
            List<PlannedInternship> previousSemesterInternships) {
        
        log.info("\n========== PHASE 1: Teacher & School Assignment (with preservation) ==========");
        log.info("Input: {} teachers, {} schools, {} students", 
                 teachers.size(), schools.size(), studentConfigs.size());
        log.info("Previous semester assignments: {}", 
                 previousSemesterInternships != null ? previousSemesterInternships.size() : 0);
        log.info("Total internship slots budget: {}\n", timeBudget);
        
        // Create internship slots
        List<PlannedInternship> plannedInternships = createPlannedInternshipsFromDemand(
                studentConfigs, schoolYear);
        
        // Build distributions and coordinates
        ZspCourseDistribution zspDistribution = buildZspCourseDistribution(studentConfigs);
        List<CoordinatesDto> pdpGsStudentCoords = buildPdpStudentCoords(studentConfigs, SchoolType.GS);
        List<CoordinatesDto> pdpMsStudentCoords = buildPdpStudentCoords(studentConfigs, SchoolType.MS);
        
        log.info("Created {} internship slots\n", plannedInternships.size());
        
        // Fetch all active courses
        List<Course> courses = courseRepository.findByActiveTrue();
        
        // Run Phase 1 with previous semester data
        InternshipSolution phase1Result = runPhase1WithPreservation(
            teachers, schools, courses, plannedInternships, schoolYear, timeBudget, 
            zspDistribution, pdpGsStudentCoords, pdpMsStudentCoords, 
            previousSemesterInternships);
        
        // Remove inactive internships
        List<PlannedInternship> activeInternships = phase1Result.getPlannedInternships().stream()
                .filter(PlannedInternship::isActive)
                .toList();
        
        // Deduplicate based on type, schoolType, course, teacher, and school
        Map<String, PlannedInternship> uniqueInternships = new LinkedHashMap<>();
        for (PlannedInternship pi : activeInternships) {
            String key = buildInternshipKey(pi);
            if (!uniqueInternships.containsKey(key)) {
                uniqueInternships.put(key, pi);
            } else {
                log.debug("Skipping duplicate internship: {}", key);
            }
        }
        
        List<PlannedInternship> deduplicatedInternships = new ArrayList<>(uniqueInternships.values());
        log.info("Deduplicated internships: {} active -> {} unique", 
                 activeInternships.size(), deduplicatedInternships.size());
        
        // Populate schools from assigned teachers
        deduplicatedInternships.forEach(PlannedInternship::populateSchoolFromTeacher);
        
        // Save unique active internships
        List<PlannedInternship> savedInternships = plannedInternshipRepository.saveAll(deduplicatedInternships);
        phase1Result.setPlannedInternships(savedInternships);
        
        long activeCount = savedInternships.stream()
                .filter(PlannedInternship::isActive)
                .count();
        
        log.info("========== PHASE 1 COMPLETE (with preservation) ==========");
        log.info("Active internships: {}/{}", activeCount, savedInternships.size());
        log.info("Score: {}\n", phase1Result.getScore());
        
        return phase1Result;
    }

    /**
     * Run Phase 1 optimization asynchronously using SolverManager.
     * This method is non-blocking and updates the job status via OptimizationJobService.
     * 
     * @param teachers Available teachers with their configurations
     * @param schools Available schools
     * @param studentConfigs Student configurations (to determine demand)
     * @param schoolYear Academic year (e.g., "2024/2025")
     * @param timeBudget Total internship slots budget
     * @param solverTimeLimitSeconds Time limit for solver in seconds (60-43200)
     * @param jobId Job ID for tracking progress
     */
    @Async("optimizationExecutor")
    @Transactional
    public void optimizeAsync(
            List<Teacher> teachers,
            List<School> schools,
            List<StudentConfig> studentConfigs,
            String schoolYear,
            Integer timeBudget,
            Long solverTimeLimitSeconds,
            String jobId) {
        
        log.info("\n========== PHASE 1 ASYNC: Teacher & School Assignment ==========");
        log.info("Job ID: {}", jobId);
        log.info("Input: {} teachers, {} schools, {} students", 
                 teachers.size(), schools.size(), studentConfigs.size());
        log.info("Solver time limit: {} seconds", solverTimeLimitSeconds);
        log.info("Total internship slots budget: {}\n", timeBudget);
        
        try {
            // Mark job as running
            jobService.markJobAsRunning(jobId);
            
            // Create internship slots (one per student per checked type)
            List<PlannedInternship> plannedInternships = createPlannedInternshipsFromDemand(
                    studentConfigs, schoolYear);
            
            // Build ZSP course distribution maps from student preferences
            ZspCourseDistribution zspDistribution = buildZspCourseDistribution(studentConfigs);

            // Build PDP student coordinate lists (GS/MS) from semester address, falling back to home address
            List<CoordinatesDto> pdpGsStudentCoords = buildPdpStudentCoords(studentConfigs, SchoolType.GS);
            List<CoordinatesDto> pdpMsStudentCoords = buildPdpStudentCoords(studentConfigs, SchoolType.MS);
            
            log.info("Created {} internship slots\n", plannedInternships.size());
            
            // Fetch all active courses for ZSP assignment
            List<Course> courses = courseRepository.findByActiveTrue();
            
            // Prepare problem
            InternshipSolution unsolvedProblem = new InternshipSolution();
            unsolvedProblem.setAvailableTeachers(teachers);
            unsolvedProblem.setAvailableCourses(courses);
            unsolvedProblem.setPlannedInternships(plannedInternships);
            unsolvedProblem.setSchoolYear(schoolYear);
            unsolvedProblem.setTimeBudget(timeBudget);
            unsolvedProblem.setBudget(new InternshipBudget(timeBudget));
            unsolvedProblem.setZspCourseDistribution(zspDistribution);
            unsolvedProblem.setPdpGsStudentCoords(pdpGsStudentCoords);
            unsolvedProblem.setPdpMsStudentCoords(pdpMsStudentCoords);
            unsolvedProblem.setPreviousSemesterInternships(new ArrayList<>()); // Initialize to empty list
            
            // Use problem ID for solver manager (schoolYear + jobType)
            String problemId = "PHASE1-" + schoolYear;
            
            // Solve asynchronously using SolverManager
            log.info("Starting solver with problemId: {} and time limit: {}s", problemId, solverTimeLimitSeconds);
            
            SolverJob<InternshipSolution, String> solverJob = solverManager.solve(
                    problemId, 
                    unsolvedProblem
            );
            
            // Wait for the solution
            InternshipSolution solution;
            try {
                solution = solverJob.getFinalBestSolution();
            } catch (Exception e) {
                log.error("Solver job failed: {}", e.getMessage(), e);
                throw new RuntimeException("Solver failed: " + e.getMessage(), e);
            }
            
            // Post-solve diagnostic
            logMinimumActivationStatus(solution.getPlannedInternships());
            
            // Remove inactive internships (solver decided we don't need them)
            List<PlannedInternship> activeInternships = solution.getPlannedInternships().stream()
                    .filter(PlannedInternship::isActive)
                    .toList();
            
            // Deduplicate based on type, schoolType, course, teacher, and school
            Map<String, PlannedInternship> uniqueInternships = new LinkedHashMap<>();
            for (PlannedInternship pi : activeInternships) {
                String key = buildInternshipKey(pi);
                if (!uniqueInternships.containsKey(key)) {
                    uniqueInternships.put(key, pi);
                } else {
                    log.debug("Skipping duplicate internship: {}", key);
                }
            }
            
            List<PlannedInternship> deduplicatedInternships = new ArrayList<>(uniqueInternships.values());
            log.info("Deduplicated internships: {} active -> {} unique", 
                     activeInternships.size(), deduplicatedInternships.size());
            
            // Populate schools from assigned teachers (post-optimization step)
            deduplicatedInternships.forEach(PlannedInternship::populateSchoolFromTeacher);
            
            // Save only unique active internships to database
            List<PlannedInternship> savedInternships = plannedInternshipRepository.saveAll(deduplicatedInternships);
            
            long activeCount = savedInternships.stream()
                    .filter(PlannedInternship::isActive)
                    .count();
            
            log.info("========== PHASE 1 ASYNC COMPLETE ==========");
            log.info("Active internships: {}/{}", activeCount, savedInternships.size());
            log.info("Score: {}\n", solution.getScore());
            
            // Mark job as completed with success message
            String message = String.format("Phase 1 complete: %d/%d internships assigned teachers. Score: %s",
                    activeCount, savedInternships.size(), solution.getScore());
            jobService.markJobAsCompleted(jobId, message);
            
        } catch (Exception e) {
            log.error("Phase 1 async optimization failed for job {}: {}", jobId, e.getMessage(), e);
            jobService.markJobAsFailed(jobId, "Optimization failed: " + e.getMessage());
        }
    }

    /**
     * Run Phase 1 optimization with some internships already assigned (fixed) and others to be optimized.
     * Used during reoptimization when we want to preserve existing assignments and only optimize for additional demand.
     * 
     * @param teachers Available teachers with their configurations
     * @param schools Available schools
     * @param allInternships Combined list of fixed internships (with teachers assigned) and new slots (teacher=null)
     * @param studentConfigs Student configurations (to determine demand)
     * @param schoolYear Academic year (e.g., "SoSe2025")
     * @param timeBudget Total internship slots budget
     * @return Phase 1 solution with all internships (fixed + optimized)
     */
    @Transactional
    public InternshipSolution optimizeWithFixedInternships(
            List<Teacher> teachers,
            List<School> schools,
            List<PlannedInternship> allInternships,
            List<StudentConfig> studentConfigs,
            String schoolYear,
            Integer timeBudget) {
        
        log.info("\n========== PHASE 1: Teacher & School Assignment (with fixed assignments) ==========");
        log.info("Input: {} teachers, {} schools, {} students", 
                 teachers.size(), schools.size(), studentConfigs.size());
        
        // Separate fixed and new internships
        List<PlannedInternship> fixedInternships = allInternships.stream()
                .filter(pi -> pi.getAssignedTeacher() != null)
                .toList();
        List<PlannedInternship> newInternships = allInternships.stream()
                .filter(pi -> pi.getAssignedTeacher() == null)
                .toList();
        
        log.info("Fixed internships (already assigned): {}", fixedInternships.size());
        log.info("New internships (to be optimized): {}", newInternships.size());
        log.info("Total internship slots budget: {}\n", timeBudget);
        
        // Build distributions and coordinates
        ZspCourseDistribution zspDistribution = buildZspCourseDistribution(studentConfigs);
        List<CoordinatesDto> pdpGsStudentCoords = buildPdpStudentCoords(studentConfigs, SchoolType.GS);
        List<CoordinatesDto> pdpMsStudentCoords = buildPdpStudentCoords(studentConfigs, SchoolType.MS);
        
        // Fetch all active courses
        List<Course> courses = courseRepository.findByActiveTrue();
        
        // Run Phase 1 only on new internships
        InternshipSolution phase1Result = runPhase1WithFixed(
            teachers, schools, courses, newInternships, fixedInternships, schoolYear, timeBudget, 
            zspDistribution, pdpGsStudentCoords, pdpMsStudentCoords);
        
        // Combine fixed and newly assigned internships
        List<PlannedInternship> allOptimizedInternships = new ArrayList<>();
        allOptimizedInternships.addAll(fixedInternships);
        allOptimizedInternships.addAll(phase1Result.getPlannedInternships());
        
        // Filter to active internships
        List<PlannedInternship> activeInternships = allOptimizedInternships.stream()
                .filter(PlannedInternship::isActive)
                .toList();
        
        // Deduplicate based on type, schoolType, course, teacher, and school
        Map<String, PlannedInternship> uniqueInternships = new LinkedHashMap<>();
        for (PlannedInternship pi : activeInternships) {
            String key = buildInternshipKey(pi);
            if (!uniqueInternships.containsKey(key)) {
                uniqueInternships.put(key, pi);
            } else {
                log.debug("Skipping duplicate internship: {}", key);
            }
        }
        
        List<PlannedInternship> deduplicatedInternships = new ArrayList<>(uniqueInternships.values());
        log.info("Deduplicated internships: {} active -> {} unique", 
                 activeInternships.size(), deduplicatedInternships.size());
        
        // Populate schools from assigned teachers
        deduplicatedInternships.forEach(PlannedInternship::populateSchoolFromTeacher);
        
        // Save all unique active internships
        List<PlannedInternship> savedInternships = plannedInternshipRepository.saveAll(deduplicatedInternships);
        phase1Result.setPlannedInternships(savedInternships);
        
        long activeCount = savedInternships.stream()
                .filter(PlannedInternship::isActive)
                .count();
        
        log.info("========== PHASE 1 COMPLETE (with fixed assignments) ==========");
        log.info("Fixed internships: {}", fixedInternships.size());
        log.info("Newly optimized internships: {}/{}", 
                 newInternships.stream().filter(PlannedInternship::isActive).count(), 
                 newInternships.size());
        log.info("Total active internships: {}/{}", activeCount, savedInternships.size());
        log.info("Score: {}\n", phase1Result.getScore());
        
        return phase1Result;
    }

    /**
     * PHASE 1: Activate internship slots and assign teachers/schools.
     */
        private InternshipSolution runPhase1(
            List<Teacher> teachers,
            List<School> schools,
            List<Course> courses,
            List<PlannedInternship> plannedInternships,
            String schoolYear,
            Integer timeBudget,
            ZspCourseDistribution zspDistribution,
            List<CoordinatesDto> pdpGsStudentCoords,
            List<CoordinatesDto> pdpMsStudentCoords) {
        
        // Create solver for Phase 1
        SolverFactory<InternshipSolution> solverFactory = 
                SolverFactory.createFromXmlResource("solverConfig.xml");
        Solver<InternshipSolution> solver = solverFactory.buildSolver();
        
        // Prepare problem
        InternshipSolution unsolvedProblem = new InternshipSolution();
        unsolvedProblem.setAvailableTeachers(teachers);
        unsolvedProblem.setAvailableCourses(courses);
        unsolvedProblem.setPlannedInternships(plannedInternships);
        unsolvedProblem.setSchoolYear(schoolYear);
        unsolvedProblem.setTimeBudget(timeBudget);
        unsolvedProblem.setBudget(new InternshipBudget(timeBudget));
        unsolvedProblem.setZspCourseDistribution(zspDistribution);
        unsolvedProblem.setPdpGsStudentCoords(pdpGsStudentCoords);
        unsolvedProblem.setPdpMsStudentCoords(pdpMsStudentCoords);
        unsolvedProblem.setPreviousSemesterInternships(new ArrayList<>()); // Initialize to empty list
        
        // Solve
        InternshipSolution solution = solver.solve(unsolvedProblem);

        // Post-solve diagnostic
        logMinimumActivationStatus(solution.getPlannedInternships());
        
        return solution;
    }
    
    /**
     * PHASE 1 with fixed internships: Only optimize new internships, keeping fixed ones unchanged.
     */
    private InternshipSolution runPhase1WithFixed(
            List<Teacher> teachers,
            List<School> schools,
            List<Course> courses,
            List<PlannedInternship> newInternships,
            List<PlannedInternship> fixedInternships,
            String schoolYear,
            Integer timeBudget,
            ZspCourseDistribution zspDistribution,
            List<CoordinatesDto> pdpGsStudentCoords,
            List<CoordinatesDto> pdpMsStudentCoords) {
        
        // Create solver for Phase 1
        SolverFactory<InternshipSolution> solverFactory = 
                SolverFactory.createFromXmlResource("solverConfig.xml");
        Solver<InternshipSolution> solver = solverFactory.buildSolver();
        
        // Prepare problem - only optimize new internships
        InternshipSolution unsolvedProblem = new InternshipSolution();
        unsolvedProblem.setAvailableTeachers(teachers);
        unsolvedProblem.setAvailableCourses(courses);
        unsolvedProblem.setPlannedInternships(newInternships); // Only new internships to optimize
        unsolvedProblem.setSchoolYear(schoolYear);
        unsolvedProblem.setTimeBudget(timeBudget);
        unsolvedProblem.setBudget(new InternshipBudget(timeBudget));
        unsolvedProblem.setZspCourseDistribution(zspDistribution);
        unsolvedProblem.setPdpGsStudentCoords(pdpGsStudentCoords);
        unsolvedProblem.setPdpMsStudentCoords(pdpMsStudentCoords);
        
        // Add fixed internships as previous semester data for teacher tracking (budget calculation)
        unsolvedProblem.setPreviousSemesterInternships(fixedInternships);
        
        // Solve
        InternshipSolution solution = solver.solve(unsolvedProblem);

        // Post-solve diagnostic
        logMinimumActivationStatus(solution.getPlannedInternships());
        
        return solution;
    }
    
    /**
     * PHASE 1 with teacher preservation: Activate internship slots and assign teachers/schools.
     * Includes previous semester data to reward preserving teacher assignments.
     */
    private InternshipSolution runPhase1WithPreservation(
            List<Teacher> teachers,
            List<School> schools,
            List<Course> courses,
            List<PlannedInternship> plannedInternships,
            String schoolYear,
            Integer timeBudget,
            ZspCourseDistribution zspDistribution,
            List<CoordinatesDto> pdpGsStudentCoords,
            List<CoordinatesDto> pdpMsStudentCoords,
            List<PlannedInternship> previousSemesterInternships) {
        
        // Create solver for Phase 1
        SolverFactory<InternshipSolution> solverFactory = 
                SolverFactory.createFromXmlResource("solverConfig.xml");
        Solver<InternshipSolution> solver = solverFactory.buildSolver();
        
        // Prepare problem with previous semester data
        InternshipSolution unsolvedProblem = new InternshipSolution();
        unsolvedProblem.setAvailableTeachers(teachers);
        unsolvedProblem.setAvailableCourses(courses);
        unsolvedProblem.setPlannedInternships(plannedInternships);
        unsolvedProblem.setSchoolYear(schoolYear);
        unsolvedProblem.setTimeBudget(timeBudget);
        unsolvedProblem.setBudget(new InternshipBudget(timeBudget));
        unsolvedProblem.setZspCourseDistribution(zspDistribution);
        unsolvedProblem.setPdpGsStudentCoords(pdpGsStudentCoords);
        unsolvedProblem.setPdpMsStudentCoords(pdpMsStudentCoords);
        unsolvedProblem.setPreviousSemesterInternships(previousSemesterInternships); // ADD PRESERVATION DATA
        
        // Solve
        InternshipSolution solution = solver.solve(unsolvedProblem);

        // Post-solve diagnostic: verify minimum-activation constraint status
        logMinimumActivationStatus(solution.getPlannedInternships());
        
        return solution;
    }

    /**
     * Collects PDP student coordinates for a given school type.
     * Prefers semester address; falls back to home address; skips if none.
     */
    private List<CoordinatesDto> buildPdpStudentCoords(List<StudentConfig> studentConfigs, SchoolType targetType) {
        List<CoordinatesDto> coords = new ArrayList<>();
        for (StudentConfig config : studentConfigs) {
            if (!(config.isPdpI() || config.isPdpII())) {
                continue;
            }
            if (config.getSchoolType() != targetType) {
                continue;
            }
            Student student = config.getStudent();
            if (student == null) {
                continue;
            }
            // Prefer semester address
            Address sem = student.getAddressSemester();
            if (sem != null && sem.getLatitude() != null && sem.getLongitude() != null) {
                coords.add(new CoordinatesDto(sem.getLongitude(), sem.getLatitude()));
                continue;
            }
            // Fall back to primary address
            Address home = student.getAddress();
            if (home != null && home.getLatitude() != null && home.getLongitude() != null) {
                coords.add(new CoordinatesDto(home.getLongitude(), home.getLatitude()));
            }
        }
        return coords;
    }

    /**
     * Creates one planned internship slot per student per checked internship type.
     * 
     * Logic:
     * - PDP_I/II: Create slot with no course
     * - SFP: Create slot with student's main course
     * - ZSP: Create slot with NO course (course assignment will be guided by weighted preference maps)
     * 
     * For ZSP, we build weighted course distribution maps (GS and MS separately):
     * - Main course: 0.5
     * - Pref1: 0.3
     * - Pref2: 0.15
     * - Pref3: 0.05
     * These maps will be used as soft constraint targets.
     */
    private List<PlannedInternship> createPlannedInternshipsFromDemand(
            List<StudentConfig> studentConfigs,
            String schoolYear) {
        
        List<PlannedInternship> internships = new ArrayList<>();
        
        // Create one slot per student per checked internship type
        for (StudentConfig config : studentConfigs) {
            SchoolType schoolType = config.getSchoolType();
            
            if (config.isPdpI()) {
                internships.add(createInternshipSlot(
                        PraktikumType.PDP_I, schoolType, null, schoolYear, PDP_CAPACITY));
            }
            
            if (config.isPdpII()) {
                internships.add(createInternshipSlot(
                        PraktikumType.PDP_II, schoolType, null, schoolYear, PDP_CAPACITY));
            }
            
            if (config.isZsp()) {
                internships.add(createInternshipSlot(
                        PraktikumType.ZSP, schoolType, null, schoolYear, ZSP_CAPACITY));
            }
            
            if (config.isSfp()) {
                internships.add(createInternshipSlot(
                        PraktikumType.SFP, schoolType, config.getMainCourse(), schoolYear, SFP_CAPACITY));
            }
        }
        
        return internships;
    }

    /**
     * Builds weighted course distribution maps for ZSP preferences.
     * 
     * For each student with ZSP checked, adds weighted preferences to the appropriate map (GS or MS):
     * - Main course: 0.5
     * - Preference 1: 0.3
     * - Preference 2: 0.15
     * - Preference 3: 0.05
     */
    private ZspCourseDistribution buildZspCourseDistribution(List<StudentConfig> studentConfigs) {
        Map<Course, Double> gsDistribution = new HashMap<>();
        Map<Course, Double> msDistribution = new HashMap<>();
        
        for (StudentConfig config : studentConfigs) {
            if (!config.isZsp()) {
                continue;
            }
            
            Map<Course, Double> targetMap = config.getSchoolType() == SchoolType.GS 
                    ? gsDistribution 
                    : msDistribution;
            
            if (config.getMainCourse() != null) {
                targetMap.merge(config.getMainCourse(), 0.5, Double::sum);
            }
            if (config.getPrefCourse1() != null) {
                targetMap.merge(config.getPrefCourse1(), 0.3, Double::sum);
            }
            if (config.getPrefCourse2() != null) {
                targetMap.merge(config.getPrefCourse2(), 0.15, Double::sum);
            }
            if (config.getPrefCourse3() != null) {
                targetMap.merge(config.getPrefCourse3(), 0.05, Double::sum);
            }
        }
        
        return new ZspCourseDistribution(gsDistribution, msDistribution);
    }

        private PlannedInternship createInternshipSlot(
            PraktikumType type,
            SchoolType schoolType,
            Course course,
            String schoolYear,
            int capacity) {
        return PlannedInternship.builder()
            .praktikumType(type)
            .schoolType(schoolType)
            .course(course)
            .originalCourse(type == PraktikumType.SFP ? course : null) // Pin SFP courses
            .schoolYear(schoolYear)
            .maxCapacity(capacity)
            .currentAssignments(0)
            .active(Boolean.FALSE)
            .assignedTeacher(null)
            .build();
    }

    /**
     * Post-solve diagnostic: verify minimum-activation constraint status in final solution.
     */
    private void logMinimumActivationStatus(List<PlannedInternship> internships) {
        log.info("\n========== MINIMUM ACTIVATION DIAGNOSTIC ==========");
        
        // PDP_I per school type
        Map<SchoolType, Long> pdpITotal = new HashMap<>();
        Map<SchoolType, Long> pdpIActive = new HashMap<>();
        internships.stream()
            .filter(i -> i.getPraktikumType() == PraktikumType.PDP_I)
            .forEach(i -> {
                pdpITotal.merge(i.getSchoolType(), 1L, Long::sum);
                if (i.isActive()) {
                    pdpIActive.merge(i.getSchoolType(), 1L, Long::sum);
                }
            });
        pdpITotal.forEach((schoolType, total) -> {
            long active = pdpIActive.getOrDefault(schoolType, 0L);
            int required = (int) Math.ceil(total / 2.0);
            log.info("PDP_I {}: {}/{} active, required: {}, deficit: {}",
                schoolType, active, total, required, Math.max(0, required - active));
        });
        
        // PDP_II per school type
        Map<SchoolType, Long> pdpIITotal = new HashMap<>();
        Map<SchoolType, Long> pdpIIActive = new HashMap<>();
        internships.stream()
            .filter(i -> i.getPraktikumType() == PraktikumType.PDP_II)
            .forEach(i -> {
                pdpIITotal.merge(i.getSchoolType(), 1L, Long::sum);
                if (i.isActive()) {
                    pdpIIActive.merge(i.getSchoolType(), 1L, Long::sum);
                }
            });
        pdpIITotal.forEach((schoolType, total) -> {
            long active = pdpIIActive.getOrDefault(schoolType, 0L);
            int required = (int) Math.ceil(total / 2.0);
            log.info("PDP_II {}: {}/{} active, required: {}, deficit: {}",
                schoolType, active, total, required, Math.max(0, required - active));
        });
        
        // ZSP per school type
        Map<SchoolType, Long> zspTotal = new HashMap<>();
        Map<SchoolType, Long> zspActive = new HashMap<>();
        internships.stream()
            .filter(i -> i.getPraktikumType() == PraktikumType.ZSP)
            .forEach(i -> {
                zspTotal.merge(i.getSchoolType(), 1L, Long::sum);
                if (i.isActive()) {
                    zspActive.merge(i.getSchoolType(), 1L, Long::sum);
                }
            });
        zspTotal.forEach((schoolType, total) -> {
            long active = zspActive.getOrDefault(schoolType, 0L);
            int required = (int) Math.ceil(total / 4.0);
            log.info("ZSP {}: {}/{} active, required: {}, deficit: {}",
                schoolType, active, total, required, Math.max(0, required - active));
        });
        
        // SFP per school type + course
        Map<String, Long> sfpTotal = new HashMap<>();
        Map<String, Long> sfpActive = new HashMap<>();
        internships.stream()
            .filter(i -> i.getPraktikumType() == PraktikumType.SFP && i.getCourse() != null)
            .forEach(i -> {
                String key = i.getSchoolType() + "/" + i.getCourse().getName();
                sfpTotal.merge(key, 1L, Long::sum);
                if (i.isActive()) {
                    sfpActive.merge(key, 1L, Long::sum);
                }
            });
        sfpTotal.forEach((key, total) -> {
            long active = sfpActive.getOrDefault(key, 0L);
            int required = (int) Math.ceil(total / 4.0);
            log.info("SFP {}: {}/{} active, required: {}, deficit: {}",
                key, active, total, required, Math.max(0, required - active));
        });
        
        log.info("====================================================\n");
    }
    
    /**
     * Build unique key for deduplication.
     * Key includes: type, schoolType, course (if SFP), teacher, and school
     */
    private String buildInternshipKey(PlannedInternship pi) {
        StringBuilder key = new StringBuilder();
        key.append(pi.getPraktikumType());
        key.append("/").append(pi.getSchoolType());
        
        if (pi.getPraktikumType() == PraktikumType.SFP && pi.getCourse() != null) {
            key.append("/").append(pi.getCourse().getId());
        }
        
        if (pi.getAssignedTeacher() != null) {
            key.append("/T").append(pi.getAssignedTeacher().getTeacherId());
        }
        
        if (pi.getAssignedSchool() != null) {
            key.append("/S").append(pi.getAssignedSchool().getId());
        }
        
        return key.toString();
    }
}
