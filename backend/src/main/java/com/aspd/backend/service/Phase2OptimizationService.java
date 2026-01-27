package com.aspd.backend.service;

import com.aspd.backend.model.AssignmentStatus;
import com.aspd.backend.model.BaselineAssignment;
import com.aspd.backend.model.Course;
import com.aspd.backend.model.InternshipAssignment;
import com.aspd.backend.model.PlannedInternship;
import com.aspd.backend.model.PraktikumType;
import com.aspd.backend.model.StudentConfig;
import com.aspd.backend.model.StudentInternshipDemand;
import com.aspd.backend.optimization.OptimizationJobService;
import com.aspd.backend.repository.BaselineAssignmentRepository;
import com.aspd.backend.repository.InternshipAssignmentRepository;
import com.aspd.backend.repository.PlannedInternshipRepository;
import com.aspd.backend.repository.StudentInternshipDemandRepository;
import com.aspd.backend.solver.StudentAssignmentSolution;
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
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service for Phase 2 optimization: Assign students to planned internships.
 */
@Service
@Slf4j
public class Phase2OptimizationService {

    private final PlannedInternshipRepository plannedInternshipRepository;
    private final StudentInternshipDemandRepository studentInternshipDemandRepository;
    private final BaselineAssignmentRepository baselineAssignmentRepository;
    private final InternshipAssignmentRepository assignmentRepository;
    private final SolverManager<StudentAssignmentSolution, String> solverManager;
    private final OptimizationJobService jobService;

    public Phase2OptimizationService(
            PlannedInternshipRepository plannedInternshipRepository,
            StudentInternshipDemandRepository studentInternshipDemandRepository,
            BaselineAssignmentRepository baselineAssignmentRepository,
            InternshipAssignmentRepository assignmentRepository,
            @Qualifier("phase2SolverManager") SolverManager<StudentAssignmentSolution, String> solverManager,
            OptimizationJobService jobService) {
        this.plannedInternshipRepository = plannedInternshipRepository;
        this.studentInternshipDemandRepository = studentInternshipDemandRepository;
        this.baselineAssignmentRepository = baselineAssignmentRepository;
        this.assignmentRepository = assignmentRepository;
        this.solverManager = solverManager;
        this.jobService = jobService;
    }

    /**
     * Optimize Phase 2: Student assignment.
     * Loads planned internships from Phase 1 and optimizes student assignments.
     * 
     * @param studentConfigs Student configurations
     * @param schoolYear Academic year (e.g., "2024/2025")
     * @param timeBudget Total hours budget (optional, for validation)
     * @return Phase 2 solution with optimized student assignments
     */
    @Transactional
    public StudentAssignmentSolution optimize(
            List<StudentConfig> studentConfigs,
            String schoolYear,
            Integer timeBudget) {
        return optimize(studentConfigs, schoolYear, timeBudget, null);
    }

    /**
     * Optimize Phase 2 with optional baseline preservation.
     * 
     * @param studentConfigs Student configurations
     * @param schoolYear Academic year
     * @param timeBudget Total hours budget
     * @param semester Semester for baseline lookup (null = no baseline)
     * @return Phase 2 solution with optimized student assignments
     */
    @Transactional
    public StudentAssignmentSolution optimize(
            List<StudentConfig> studentConfigs,
            String schoolYear,
            Integer timeBudget,
            String semester) {
        
        log.info("\n========== PHASE 2: Student Assignment ==========");
        if (semester != null) {
            log.info("Re-optimization mode: using baseline from semester={}", semester);
        }
        
        // Load and validate planned internships from Phase 1
        List<PlannedInternship> plannedInternships = loadAndValidatePlannedInternships(schoolYear);
        
        log.info("Loaded {} planned internships from Phase 1", plannedInternships.size());
        log.info("Processing {} students\n", studentConfigs.size());
        
        // Create student demands
        List<StudentInternshipDemand> studentDemands = createStudentDemandsFromConfigs(
                studentConfigs, schoolYear);
        
        // Apply baseline if semester is specified
        if (semester != null) {
            applyBaselineToDemandsInternships(studentDemands, plannedInternships, schoolYear, semester);
        }
        
        // Run Phase 2
        StudentAssignmentSolution phase2Result = runPhase2(
                plannedInternships, studentDemands, schoolYear, timeBudget);
        
        // Save and finalize results
        return saveAndFinalizeResults(phase2Result);
    }

    /**
     * Optimize Phase 2 asynchronously using SolverManager.
     * This method is non-blocking and updates the job status via OptimizationJobService.
     * Fixed time limit of 120 seconds.
     * 
     * @param studentConfigs Student configurations
     * @param schoolYear Academic year
     * @param timeBudget Total hours budget
     * @param jobId Job ID for tracking progress
     */
    @Async("optimizationExecutor")
    @Transactional
    public void optimizeAsync(
            List<StudentConfig> studentConfigs,
            String schoolYear,
            Integer timeBudget,
            String jobId) {
        
        log.info("\n========== PHASE 2 ASYNC: Student Assignment ==========");
        log.info("Job ID: {}", jobId);
        log.info("Solver time limit: 120 seconds (fixed)");
        
        try {
            // Mark job as running
            jobService.markJobAsRunning(jobId);
            
            // Load and validate planned internships from Phase 1
            List<PlannedInternship> plannedInternships = loadAndValidatePlannedInternships(schoolYear);
            
            log.info("Loaded {} planned internships from Phase 1", plannedInternships.size());
            log.info("Processing {} students\n", studentConfigs.size());
            
            // Create student demands
            List<StudentInternshipDemand> studentDemands = createStudentDemandsFromConfigs(
                    studentConfigs, schoolYear);
            
            // Create unified solution with all demands and internships
            StudentAssignmentSolution problem = createSolution(
                    plannedInternships, studentDemands, schoolYear, timeBudget);
            
            // Use problem ID for solver manager (schoolYear + jobType)
            String problemId = "PHASE2-" + schoolYear;
            
            // Solve asynchronously using SolverManager (fixed 120 seconds)
            log.info("Starting solver with problemId: {}", problemId);
            
            SolverJob<StudentAssignmentSolution, String> solverJob = solverManager.solve(
                    problemId,
                    problem
            );
            
            // Wait for the solution
            StudentAssignmentSolution solution;
            try {
                solution = solverJob.getFinalBestSolution();
            } catch (Exception e) {
                log.error("Solver job failed: {}", e.getMessage(), e);
                throw new RuntimeException("Solver failed: " + e.getMessage(), e);
            }
            
            long assignedCount = solution.getStudentDemands().stream()
                    .filter(d -> d.getAssignedInternship() != null)
                    .count();
            
            log.info("Student assignments: {}/{}", assignedCount, studentDemands.size());
            log.info("Score: {}", solution.getScore());
            
            // Save and finalize results
            List<StudentInternshipDemand> savedDemands = studentInternshipDemandRepository.saveAll(
                    solution.getStudentDemands());
            
            solution.setStudentDemands(savedDemands);
            
            // Create and save final InternshipAssignment records
            List<InternshipAssignment> finalAssignments = createFinalAssignments(
                    savedDemands, schoolYear);
            assignmentRepository.saveAll(finalAssignments);
            
            log.info("========== PHASE 2 ASYNC COMPLETE ==========");
            log.info("Assigned: {}/{}", assignedCount, savedDemands.size());
            log.info("Saved {} InternshipAssignment records", finalAssignments.size());
            log.info("Score: {}\n", solution.getScore());
            
            // Mark job as completed with success message
            String message = String.format("Phase 2 complete: %d/%d students assigned. Score: %s",
                    assignedCount, savedDemands.size(), solution.getScore());
            jobService.markJobAsCompleted(jobId, message);
            
        } catch (Exception e) {
            log.error("Phase 2 async optimization failed for job {}: {}", jobId, e.getMessage(), e);
            jobService.markJobAsFailed(jobId, "Optimization failed: " + e.getMessage());
        }
    }

    private List<PlannedInternship> loadAndValidatePlannedInternships(String schoolYear) {
        List<PlannedInternship> plannedInternships = plannedInternshipRepository
                .findBySchoolYear(schoolYear);
        
        if (plannedInternships.isEmpty()) {
            log.error("No planned internships found for year {}. Run Phase 1 first!", schoolYear);
            throw new IllegalStateException("Phase 1 must be run before Phase 2. No planned internships found.");
        }
        
        return plannedInternships;
    }

    private StudentAssignmentSolution saveAndFinalizeResults(StudentAssignmentSolution phase2Result) {
        List<StudentInternshipDemand> savedDemands = studentInternshipDemandRepository.saveAll(
                phase2Result.getStudentDemands());
        
        phase2Result.setStudentDemands(savedDemands);
        
        long assignedCount = savedDemands.stream()
                .filter(d -> d.getAssignedInternship() != null)
                .count();
        
        log.info("========== PHASE 2 COMPLETE ==========");
        log.info("Assigned: {}/{}", assignedCount, savedDemands.size());
        log.info("Score: {}\n", phase2Result.getScore());
        
        return phase2Result;
    }

    /**
     * PHASE 2: Assign students to planned internships.
     * Uses a single unified solver leveraging Phase 1 guarantees.
     */
    private StudentAssignmentSolution runPhase2(
            List<PlannedInternship> plannedInternships,
            List<StudentInternshipDemand> studentDemands,
            String schoolYear,
            Integer timeBudget) {
        
        // Create solver factory
        SolverFactory<StudentAssignmentSolution> solverFactory = 
                SolverFactory.createFromXmlResource("studentAssignmentSolverConfig.xml");
        
        Solver<StudentAssignmentSolution> solver = solverFactory.buildSolver();
        
        // Create unified solution with all demands and internships
        StudentAssignmentSolution problem = createSolution(
                plannedInternships, studentDemands, schoolYear, timeBudget);
        
        StudentAssignmentSolution solution = solver.solve(problem);
        
        long assignedCount = solution.getStudentDemands().stream()
                .filter(d -> d.getAssignedInternship() != null)
                .count();
        
        log.info("Student assignments: {}/{}", assignedCount, studentDemands.size());
        log.info("Score: {}", solution.getScore());
        
        return solution;
    }

    private StudentAssignmentSolution createSolution(
            List<PlannedInternship> internships,
            List<StudentInternshipDemand> demands,
            String schoolYear,
            Integer timeBudget) {
        
        StudentAssignmentSolution solution = new StudentAssignmentSolution();
        solution.setAvailableInternships(internships);
        solution.setStudentDemands(demands);
        solution.setSchoolYear(schoolYear);
        solution.setTimeBudget(timeBudget);
        return solution;
    }

    /**
     * Creates student demands from student configurations.
     */
    private List<StudentInternshipDemand> createStudentDemandsFromConfigs(
            List<StudentConfig> studentConfigs,
            String schoolYear) {
        
        List<StudentInternshipDemand> demands = new ArrayList<>();
        
        int pdp1Count = 0;
        int pdp2Count = 0;
        int zspCount = 0;
        int sfpCount = 0;
        
        for (StudentConfig config : studentConfigs) {
            pdp1Count += addDemandIfNeeded(demands, config, config.isPdpI(), PraktikumType.PDP_I, schoolYear);
            pdp2Count += addDemandIfNeeded(demands, config, config.isPdpII(), PraktikumType.PDP_II, schoolYear);
            zspCount += addDemandIfNeeded(demands, config, config.isZsp(), PraktikumType.ZSP, schoolYear);
            sfpCount += addDemandIfNeeded(demands, config, config.isSfp(), PraktikumType.SFP, schoolYear);
        }
        
        log.info("Student demands created - PDP_I: {}, PDP_II: {}, ZSP: {}, SFP: {}, TOTAL: {}",
                pdp1Count, pdp2Count, zspCount, sfpCount, demands.size());
        
        return demands;
    }

    private int addDemandIfNeeded(
            List<StudentInternshipDemand> demands,
            StudentConfig config,
            boolean shouldAdd,
            PraktikumType type,
            String schoolYear) {
        
        if (shouldAdd) {
            demands.add(createStudentDemand(config, type, config.getMainCourse(), schoolYear));
            return 1;
        }
        return 0;
    }

    /**
     * Creates a single student demand.
     */
    private StudentInternshipDemand createStudentDemand(
            StudentConfig config,
            PraktikumType type,
            Course preferredCourse,
            String schoolYear) {
        
        return StudentInternshipDemand.builder()
                .studentConfig(config)
                .praktikumType(type)
                .preferredCourse(preferredCourse)
                .schoolYear(schoolYear)
                .build();
    }

    /**
     * Creates final InternshipAssignment records from solved student demands.
     * Only creates assignments where the student has been assigned to an internship
     * that has both a teacher and school assigned.
     */
    public List<InternshipAssignment> createFinalAssignments(
            List<StudentInternshipDemand> studentDemands,
            String schoolYear) {
        
        return studentDemands.stream()
                .filter(demand -> demand.getAssignedInternship() != null)
                .filter(this::hasTeacherAndSchool)
                .map(demand -> buildInternshipAssignment(demand, schoolYear))
                .collect(Collectors.toList());
    }

        private boolean hasTeacherAndSchool(StudentInternshipDemand demand) {
                PlannedInternship internship = demand.getAssignedInternship();
                return internship.getAssignedTeacher() != null && internship.getSchool() != null;
        }

    private InternshipAssignment buildInternshipAssignment(StudentInternshipDemand demand, String schoolYear) {
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
     * Applies baseline assignments to student demands and internships for re-optimization.
     * This pre-populates the solution with existing valid assignments and pins them
     * so OptaPlanner preserves them unless necessary to change.
     * 
     * For summer re-optimization: loads winter baseline from same year
     */
    private void applyBaselineToDemandsInternships(
            List<StudentInternshipDemand> demands,
            List<PlannedInternship> internships,
            String schoolYear,
            String semester) {
        
        // Determine baseline semester to load
        // For summer (SoSe), load winter (WiSe) baseline from previous academic year
        // For winter (WiSe), load summer (SoSe) baseline from previous calendar year
        String baselineYear;
        if ("summer".equalsIgnoreCase(semester)) {
            // Loading for summer (SoSe26) -> use winter baseline from previous academic year (WiSe25-26)
            if (schoolYear.startsWith("SoSe")) {
                String year = schoolYear.substring(4);
                int yearNum = Integer.parseInt(year);
                int prevYear = yearNum - 1;
                baselineYear = String.format("WiSe%02d-%02d", prevYear, yearNum);
            } else {
                baselineYear = schoolYear;  // fallback
            }
        } else {
            // Loading for winter (WiSe25-26) -> use summer baseline from previous calendar year (SoSe25)
            if (schoolYear.startsWith("WiSe")) {
                // Extract the first year from WiSe25-26 format
                String yearPart = schoolYear.substring(4);
                String firstYear = yearPart.split("-")[0];
                baselineYear = "SoSe" + firstYear;
            } else {
                baselineYear = schoolYear;  // fallback
            }
        }
        
        // Get baseline from previous semester
        List<BaselineAssignment> baselines = baselineAssignmentRepository
                .findBySchoolYear(baselineYear);
        
        if (baselines.isEmpty()) {
            log.warn("No baseline found for year={}. Running fresh optimization.", baselineYear);
            return;
        }
        
        log.info("Found {} baseline assignments from year={}", baselines.size(), baselineYear);
        
        // Create maps for quick lookup by student matriculation number + praktikum type
        Map<String, StudentInternshipDemand> demandMap = new HashMap<>();
        for (StudentInternshipDemand demand : demands) {
            int matricNbr = demand.getStudentConfig().getStudent().getMatriculationNbr();
            String key = matricNbr + "_" + demand.getPraktikumType();
            demandMap.put(key, demand);
        }
        
        // Create map for quick lookup by teacher + school + praktikum type + course
        Map<String, PlannedInternship> internshipMap = new HashMap<>();
        for (PlannedInternship internship : internships) {
            if (internship.getAssignedTeacher() != null && internship.getSchool() != null) {
                String key = internship.getAssignedTeacher().getTeacherId() + "_" + 
                            internship.getSchool().getId() + "_" +
                            internship.getPraktikumType() + "_" +
                            (internship.getCourse() != null ? internship.getCourse().getId() : "NULL");
                internshipMap.put(key, internship);
            }
        }
        
        int appliedCount = 0;
        int pinnedCount = 0;
        
        // Apply baseline assignments
        for (BaselineAssignment baseline : baselines) {
            int matricNbr = baseline.getStudentDemand().getStudentConfig().getStudent().getMatriculationNbr();
            PraktikumType praktikumType = baseline.getStudentDemand().getPraktikumType();
            
            // Find matching demand in current demands (same student + same praktikum type)
            String demandKey = matricNbr + "_" + praktikumType;
            StudentInternshipDemand demand = demandMap.get(demandKey);
            if (demand == null) {
                log.debug("No matching demand for student {} praktikum {}", matricNbr, praktikumType);
                continue;
            }
            
            // Find matching internship by teacher-school combination
            Long teacherId = baseline.getTeacher().getTeacherId();
            Long schoolId = baseline.getSchool().getId();
            Course course = baseline.getStudentDemand().getPreferredCourse();
            
            String internshipKey = teacherId + "_" + schoolId + "_" + praktikumType + "_" +
                                  (course != null ? course.getId() : "NULL");
            PlannedInternship internship = internshipMap.get(internshipKey);
            if (internship == null) {
                log.debug("No matching internship for teacher {} school {} praktikum {} course {}", 
                         teacherId, schoolId, praktikumType, course);
                continue;
            }
            
            // Pin if marked as pinned in baseline (MUST be set first)
            if (baseline.isPinned()) {
                demand.setPinned(true);
                pinnedCount++;
            }
            
            // Store baseline for score calculator to reward preservation
            demand.setBaselineInternship(internship);
            
            // Apply baseline: pre-assign internship to demand
            demand.setAssignedInternship(internship);
            appliedCount++;
        }
        
        log.info("Applied {} baseline assignments ({} pinned)", appliedCount, pinnedCount);
        log.info("OptaPlanner will preserve pinned assignments and prefer keeping others via soft constraint");
    }
}
