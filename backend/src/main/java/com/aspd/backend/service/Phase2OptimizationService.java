package com.aspd.backend.service;

import com.aspd.backend.model.AssignmentStatus;
import com.aspd.backend.model.Course;
import com.aspd.backend.model.InternshipAssignment;
import com.aspd.backend.model.PlannedInternship;
import com.aspd.backend.model.PraktikumType;
import com.aspd.backend.model.StudentConfig;
import com.aspd.backend.model.StudentInternshipDemand;
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
import java.util.List;
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
        
        log.info("\n========== PHASE 2: Student Assignment ==========");
        
        // Load and validate planned internships from Phase 1
        List<PlannedInternship> plannedInternships = loadAndValidatePlannedInternships(schoolYear);
        
        log.info("Loaded {} planned internships from Phase 1", plannedInternships.size());
        log.info("Processing {} students\n", studentConfigs.size());
        
        // Create student demands
        List<StudentInternshipDemand> studentDemands = createStudentDemandsFromConfigs(
                studentConfigs, schoolYear);
        
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
     */
    private StudentAssignmentSolution runPhase2(
            List<PlannedInternship> plannedInternships,
            List<StudentInternshipDemand> studentDemands,
            String schoolYear,
            Integer timeBudget) {
        
        // Separate demands by type
        List<StudentInternshipDemand> pdpDemands = filterPdpDemands(studentDemands);
        List<StudentInternshipDemand> zspSfpDemands = filterZspSfpDemands(studentDemands);
        
        // Filter internships by type
        List<PlannedInternship> pdpInternships = filterPdpInternships(plannedInternships);
        List<PlannedInternship> zspSfpInternships = filterZspSfpInternships(plannedInternships);
        
        // Create solver factory
        SolverFactory<StudentAssignmentSolution> solverFactory = 
                SolverFactory.createFromXmlResource("studentAssignmentSolverConfig.xml");
        
        // Solve PDP and ZSP/SFP separately
        StudentAssignmentSolution pdpSolution = solvePdpAssignments(
                solverFactory, pdpInternships, pdpDemands, schoolYear, timeBudget);
        
        StudentAssignmentSolution zspSfpSolution = solveZspSfpAssignments(
                solverFactory, zspSfpInternships, zspSfpDemands, schoolYear, timeBudget);
        
        // Merge and return results
        return mergeSolutions(plannedInternships, pdpSolution, zspSfpSolution, schoolYear, timeBudget);
    }

    private List<StudentInternshipDemand> filterPdpDemands(List<StudentInternshipDemand> studentDemands) {
        return studentDemands.stream()
                .filter(d -> d.getPraktikumType() == PraktikumType.PDP_I || 
                            d.getPraktikumType() == PraktikumType.PDP_II)
                .collect(Collectors.toList());
    }

    private List<StudentInternshipDemand> filterZspSfpDemands(List<StudentInternshipDemand> studentDemands) {
        return studentDemands.stream()
                .filter(d -> d.getPraktikumType() == PraktikumType.ZSP || 
                            d.getPraktikumType() == PraktikumType.SFP)
                .collect(Collectors.toList());
    }

    private List<PlannedInternship> filterPdpInternships(List<PlannedInternship> plannedInternships) {
        return plannedInternships.stream()
                .filter(i -> i.getPraktikumType() == PraktikumType.PDP_I || 
                            i.getPraktikumType() == PraktikumType.PDP_II)
                .collect(Collectors.toList());
    }

    private List<PlannedInternship> filterZspSfpInternships(List<PlannedInternship> plannedInternships) {
        return plannedInternships.stream()
                .filter(i -> i.getPraktikumType() == PraktikumType.ZSP || 
                            i.getPraktikumType() == PraktikumType.SFP)
                .collect(Collectors.toList());
    }

    private StudentAssignmentSolution solvePdpAssignments(
            SolverFactory<StudentAssignmentSolution> solverFactory,
            List<PlannedInternship> pdpInternships,
            List<StudentInternshipDemand> pdpDemands,
            String schoolYear,
            Integer timeBudget) {
        
        return solveAssignments(solverFactory, pdpInternships, pdpDemands, schoolYear, timeBudget, "PDP");
    }

    private StudentAssignmentSolution solveZspSfpAssignments(
            SolverFactory<StudentAssignmentSolution> solverFactory,
            List<PlannedInternship> zspSfpInternships,
            List<StudentInternshipDemand> zspSfpDemands,
            String schoolYear,
            Integer timeBudget) {
        
        return solveAssignments(solverFactory, zspSfpInternships, zspSfpDemands, schoolYear, timeBudget, "ZSP/SFP");
    }

    private StudentAssignmentSolution solveAssignments(
            SolverFactory<StudentAssignmentSolution> solverFactory,
            List<PlannedInternship> internships,
            List<StudentInternshipDemand> demands,
            String schoolYear,
            Integer timeBudget,
            String assignmentType) {
        
        Solver<StudentAssignmentSolution> solver = solverFactory.buildSolver();
        
        StudentAssignmentSolution problem = createSolution(internships, demands, schoolYear, timeBudget);
        StudentAssignmentSolution solution = solver.solve(problem);
        
        log.info("{} assignments: {}/{}", 
                assignmentType,
                solution.getStudentDemands().stream().filter(d -> d.getAssignedInternship() != null).count(),
                demands.size());
        
        return solution;
    }

    private StudentAssignmentSolution mergeSolutions(
            List<PlannedInternship> plannedInternships,
            StudentAssignmentSolution pdpSolution,
            StudentAssignmentSolution zspSfpSolution,
            String schoolYear,
            Integer timeBudget) {
        
        List<StudentInternshipDemand> allAssignedDemands = new ArrayList<>();
        allAssignedDemands.addAll(pdpSolution.getStudentDemands());
        allAssignedDemands.addAll(zspSfpSolution.getStudentDemands());
        
        StudentAssignmentSolution solution = createSolution(plannedInternships, allAssignedDemands, schoolYear, timeBudget);
        solution.setScore(HardSoftScore.of(
                pdpSolution.getScore().hardScore() + zspSfpSolution.getScore().hardScore(),
                pdpSolution.getScore().softScore() + zspSfpSolution.getScore().softScore()
        ));
        
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
        return internship.getAssignedTeacher() != null && internship.getAssignedSchool() != null;
    }

    private InternshipAssignment buildInternshipAssignment(StudentInternshipDemand demand, String schoolYear) {
        PlannedInternship internship = demand.getAssignedInternship();
        
        return InternshipAssignment.builder()
                .studentConfig(demand.getStudentConfig())
                .plannedInternship(internship)
                .teacher(internship.getAssignedTeacher())
                .school(internship.getAssignedSchool())
                .praktikumType(demand.getPraktikumType())
                .course(internship.getCourse())
                .schoolYear(schoolYear)
                .status(AssignmentStatus.PROPOSED)
                .build();
    }
}
