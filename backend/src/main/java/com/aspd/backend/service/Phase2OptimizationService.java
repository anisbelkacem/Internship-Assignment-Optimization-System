package com.aspd.backend.service;

import com.aspd.backend.model.AssignmentStatus;
import com.aspd.backend.model.BaselineAssignment;
import com.aspd.backend.model.Course;
import com.aspd.backend.model.InternshipAssignment;
import com.aspd.backend.model.PlannedInternship;
import com.aspd.backend.model.PraktikumType;
import com.aspd.backend.model.StudentConfig;
import com.aspd.backend.model.StudentInternshipDemand;
import com.aspd.backend.repository.BaselineAssignmentRepository;
import com.aspd.backend.repository.PlannedInternshipRepository;
import com.aspd.backend.repository.StudentInternshipDemandRepository;
import com.aspd.backend.solver.StudentAssignmentSolution;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.api.solver.SolverFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;

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
@RequiredArgsConstructor
public class Phase2OptimizationService {

    private final PlannedInternshipRepository plannedInternshipRepository;
    private final StudentInternshipDemandRepository studentInternshipDemandRepository;
    private final BaselineAssignmentRepository baselineAssignmentRepository;

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
     */
    private void applyBaselineToDemandsInternships(
            List<StudentInternshipDemand> demands,
            List<PlannedInternship> internships,
            String schoolYear,
            String semester) {
        
        // Get baseline for this year and semester
        List<BaselineAssignment> baselines = baselineAssignmentRepository
                .findBySchoolYearAndSemester(schoolYear, semester);
        
        if (baselines.isEmpty()) {
            log.warn("No baseline found for year={}, semester={}. Running fresh optimization.", 
                schoolYear, semester);
            return;
        }
        
        log.info("Found {} baseline assignments to apply", baselines.size());
        
        // Create maps for quick lookup
        Map<Long, StudentInternshipDemand> demandMap = demands.stream()
                .collect(Collectors.toMap(
                    d -> d.getStudentConfig().getId(),
                    d -> d,
                    (existing, replacement) -> existing // Keep first if duplicates
                ));
        
        Map<Long, PlannedInternship> internshipMap = internships.stream()
                .collect(Collectors.toMap(PlannedInternship::getId, i -> i));
        
        int appliedCount = 0;
        int pinnedCount = 0;
        
        // Apply baseline assignments
        for (BaselineAssignment baseline : baselines) {
            StudentConfig studentConfig = baseline.getStudentDemand().getStudentConfig();
            Long plannedInternshipId = baseline.getPlannedInternship().getId();
            
            // Find matching demand in current demands
            StudentInternshipDemand demand = demandMap.get(studentConfig.getId());
            if (demand == null) {
                log.debug("No matching demand for student config {}", studentConfig.getId());
                continue;
            }
            
            // Find matching internship in current internships
            PlannedInternship internship = internshipMap.get(plannedInternshipId);
            if (internship == null) {
                log.debug("Baseline internship {} no longer exists", plannedInternshipId);
                continue;
            }
            
            // Apply baseline: pre-assign internship to demand
            demand.setAssignedInternship(internship);
            appliedCount++;
            
            // Pin if marked as pinned in baseline
            if (baseline.isPinned()) {
                demand.setPinned(true);
                pinnedCount++;
            }
        }
        
        log.info("Applied {} baseline assignments ({} pinned)", appliedCount, pinnedCount);
        log.info("OptaPlanner will preserve pinned assignments and try to keep others");
    }
}

