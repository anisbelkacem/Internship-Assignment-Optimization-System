package com.aspd.backend.service;

import com.aspd.backend.model.*;
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
     * Run Phase 2: Student assignment.
     * Loads planned internships from Phase 1 and assigns students to them.
     * 
     * @param studentConfigs Student configurations
     * @param schoolYear Academic year (e.g., "2024/2025")
     * @param timeBudget Total hours budget (optional, for validation)
     * @return Phase 2 solution with student assignments
     */
    @Transactional
    public StudentAssignmentSolution optimize(
            List<StudentConfig> studentConfigs,
            String schoolYear,
            Integer timeBudget) {
        
        log.info("\n╔══════════════════════════════════════════════════════════════════════╗");
        log.info("║  PHASE 2: Student Assignment                                        ║");
        log.info("╚══════════════════════════════════════════════════════════════════════╝");
        
        // Load the PlannedInternships from Phase 1 (already saved in database)
        List<PlannedInternship> plannedInternships = plannedInternshipRepository
                .findBySchoolYear(schoolYear);
        
        if (plannedInternships.isEmpty()) {
            log.error("No planned internships found for year {}. Run Phase 1 first!", schoolYear);
            throw new IllegalStateException("Phase 1 must be run before Phase 2. No planned internships found.");
        }
        
        log.info("Loaded {} planned internships from Phase 1", plannedInternships.size());
        log.info("Processing {} students\n", studentConfigs.size());
        
        // Create student demands
        List<StudentInternshipDemand> studentDemands = createStudentDemandsFromConfigs(
                studentConfigs, schoolYear);
        
        // Run Phase 2
        StudentAssignmentSolution phase2Result = runPhase2(
                plannedInternships, studentDemands, schoolYear, timeBudget);
        
        // Save results to database
        List<StudentInternshipDemand> savedDemands = studentInternshipDemandRepository.saveAll(
                phase2Result.getStudentDemands());
        
        phase2Result.setStudentDemands(savedDemands);
        
        long assignedCount = savedDemands.stream()
                .filter(d -> d.getAssignedInternship() != null)
                .count();
        
        log.info("╔══════════════════════════════════════════════════════════════════════╗");
        log.info("║  PHASE 2 COMPLETE                                                    ║");
        log.info("║  Assigned: {}/{}                                                 ║", assignedCount, savedDemands.size());
        log.info("║  Score: {}                                          ║", phase2Result.getScore());
        log.info("╚══════════════════════════════════════════════════════════════════════╝\n");
        
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
        
        // Separate PDP and ZSP/SFP demands
        List<StudentInternshipDemand> pdpDemands = studentDemands.stream()
                .filter(d -> d.getPraktikumType() == PraktikumType.PDP_I || 
                            d.getPraktikumType() == PraktikumType.PDP_II)
                .collect(Collectors.toList());
        
        List<StudentInternshipDemand> zspSfpDemands = studentDemands.stream()
                .filter(d -> d.getPraktikumType() == PraktikumType.ZSP || 
                            d.getPraktikumType() == PraktikumType.SFP)
                .collect(Collectors.toList());
        
        // Filter internships by type
        List<PlannedInternship> pdpInternships = plannedInternships.stream()
                .filter(i -> i.getPraktikumType() == PraktikumType.PDP_I || 
                            i.getPraktikumType() == PraktikumType.PDP_II)
                .collect(Collectors.toList());
        
        List<PlannedInternship> zspSfpInternships = plannedInternships.stream()
                .filter(i -> i.getPraktikumType() == PraktikumType.ZSP || 
                            i.getPraktikumType() == PraktikumType.SFP)
                .collect(Collectors.toList());
        
        SolverFactory<StudentAssignmentSolution> solverFactory = 
                SolverFactory.createFromXmlResource("studentAssignmentSolverConfig.xml");
        
        // ========== STEP 1: SOLVE PDP ONLY ==========
        Solver<StudentAssignmentSolution> pdpSolver = solverFactory.buildSolver();
        
        StudentAssignmentSolution pdpProblem = new StudentAssignmentSolution();
        pdpProblem.setAvailableInternships(pdpInternships);
        pdpProblem.setStudentDemands(pdpDemands);
        pdpProblem.setSchoolYear(schoolYear);
        pdpProblem.setTimeBudget(timeBudget);
        
        StudentAssignmentSolution pdpSolution = pdpSolver.solve(pdpProblem);
        
        log.info("PDP assignments: {}/{}", 
                pdpSolution.getStudentDemands().stream().filter(d -> d.getAssignedInternship() != null).count(),
                pdpDemands.size());
        
        // ========== STEP 2: SOLVE ZSP/SFP ONLY ==========
        Solver<StudentAssignmentSolution> zspSfpSolver = solverFactory.buildSolver();
        
        StudentAssignmentSolution zspSfpProblem = new StudentAssignmentSolution();
        zspSfpProblem.setAvailableInternships(zspSfpInternships);
        zspSfpProblem.setStudentDemands(zspSfpDemands);
        zspSfpProblem.setSchoolYear(schoolYear);
        zspSfpProblem.setTimeBudget(timeBudget);
        
        StudentAssignmentSolution zspSfpSolution = zspSfpSolver.solve(zspSfpProblem);
        
        log.info("ZSP/SFP assignments: {}/{}", 
                zspSfpSolution.getStudentDemands().stream().filter(d -> d.getAssignedInternship() != null).count(),
                zspSfpDemands.size());
        
        // ========== MERGE RESULTS ==========
        List<StudentInternshipDemand> allAssignedDemands = new ArrayList<>();
        allAssignedDemands.addAll(pdpSolution.getStudentDemands());
        allAssignedDemands.addAll(zspSfpSolution.getStudentDemands());
        
        StudentAssignmentSolution solution = new StudentAssignmentSolution();
        solution.setAvailableInternships(plannedInternships);
        solution.setStudentDemands(allAssignedDemands);
        solution.setSchoolYear(schoolYear);
        solution.setTimeBudget(timeBudget);
        // Note: Combined score is not meaningful since they were solved separately
        solution.setScore(HardSoftScore.of(
                pdpSolution.getScore().hardScore() + zspSfpSolution.getScore().hardScore(),
                pdpSolution.getScore().softScore() + zspSfpSolution.getScore().softScore()
        ));
        
        return solution;
    }

    /**
     * Creates student demands from student configurations.
     */
    private List<StudentInternshipDemand> createStudentDemandsFromConfigs(
            List<StudentConfig> studentConfigs,
            String schoolYear) {
        
        List<StudentInternshipDemand> demands = new ArrayList<>();
        
        int pdp1Count = 0, pdp2Count = 0, zspCount = 0, sfpCount = 0;
        
        for (StudentConfig config : studentConfigs) {
            // PDP_I
            if (config.isPdpI()) {
                demands.add(createStudentDemand(config, PraktikumType.PDP_I, 
                                                config.getMainCourse(), schoolYear));
                pdp1Count++;
            }
            
            // PDP_II
            if (config.isPdpII()) {
                demands.add(createStudentDemand(config, PraktikumType.PDP_II, 
                                                config.getMainCourse(), schoolYear));
                pdp2Count++;
            }
            
            // ZSP
            if (config.isZsp()) {
                demands.add(createStudentDemand(config, PraktikumType.ZSP, 
                                                config.getMainCourse(), schoolYear));
                zspCount++;
            }
            
            // SFP
            if (config.isSfp()) {
                demands.add(createStudentDemand(config, PraktikumType.SFP, 
                                                config.getMainCourse(), schoolYear));
                sfpCount++;
            }
        }
        
        log.info("Student demands created - PDP_I: {}, PDP_II: {}, ZSP: {}, SFP: {}, TOTAL: {}",
                pdp1Count, pdp2Count, zspCount, sfpCount, demands.size());
        
        return demands;
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
                .filter(demand -> {
                    PlannedInternship internship = demand.getAssignedInternship();
                    // Only create assignment if the internship has a teacher and school
                    return internship.getAssignedTeacher() != null && 
                           internship.getAssignedSchool() != null;
                })
                .map(demand -> {
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
                })
                .collect(Collectors.toList());
    }
}
