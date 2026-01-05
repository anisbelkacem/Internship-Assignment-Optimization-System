package com.aspd.backend.service;

import com.aspd.backend.model.*;
import com.aspd.backend.repository.PlannedInternshipRepository;
import com.aspd.backend.solver.InternshipSolution;
import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.api.solver.SolverFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

import static com.aspd.backend.common.constants.InternshipConstants.*;

/**
 * Service for Phase 1 optimization: Assign teachers and schools to planned internships.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class Phase1OptimizationService {

    private final PlannedInternshipRepository plannedInternshipRepository;

    /**
     * Run Phase 1: Teacher and School assignment.
     * 
     * @param teachers Available teachers with their configurations
     * @param schools Available schools
     * @param studentConfigs Student configurations (to determine demand)
     * @param schoolYear Academic year (e.g., "2024/2025")
     * @param timeBudget Total hours budget (optional, for validation)
     * @return Phase 1 solution with teacher and school assignments
     */
    @Transactional
    public InternshipSolution optimize(
            List<Teacher> teachers,
            List<School> schools,
            List<StudentConfig> studentConfigs,
            String schoolYear,
            Integer timeBudget) {
        
        log.info("\n╔══════════════════════════════════════════════════════════════════════╗");
        log.info("║  PHASE 1: Teacher & School Assignment                               ║");
        log.info("╚══════════════════════════════════════════════════════════════════════╝");
        log.info("Input: {} teachers, {} schools, {} students\n", 
                 teachers.size(), schools.size(), studentConfigs.size());
        
        // Create planned internship slots based on student demand
        List<PlannedInternship> plannedInternships = createPlannedInternshipsFromDemand(
                studentConfigs, schoolYear);
        
        log.info("Created {} planned internship slots\n", plannedInternships.size());
        
        // Run Phase 1
        InternshipSolution phase1Result = runPhase1(
                teachers, schools, plannedInternships, schoolYear, timeBudget);
        
        // Save PlannedInternships to database
        List<PlannedInternship> savedInternships = plannedInternshipRepository.saveAll(
                phase1Result.getPlannedInternships());
        
        phase1Result.setPlannedInternships(savedInternships);
        
        long assignedCount = savedInternships.stream()
                .filter(i -> i.getAssignedTeacher() != null)
                .count();
        
        log.info("╔══════════════════════════════════════════════════════════════════════╗");
        log.info("║  PHASE 1 COMPLETE                                                    ║");
        log.info("║  Assigned: {}/{}                                                 ║", assignedCount, savedInternships.size());
        log.info("║  Score: {}                                           ║", phase1Result.getScore());
        log.info("╚══════════════════════════════════════════════════════════════════════╝\n");
        
        return phase1Result;
    }

    /**
     * PHASE 1: Assign teachers and schools to planned internships.
     */
    private InternshipSolution runPhase1(
            List<Teacher> teachers,
            List<School> schools,
            List<PlannedInternship> plannedInternships,
            String schoolYear,
            Integer timeBudget) {
        
        log.info("Running Phase 1: Teacher-to-Internship Assignment");
        log.info("Available schools: {}", schools.size());
        schools.forEach(school -> {
            log.info("  - School {} ({}) Zone {} [{}]", 
                    school.getId(), school.getName(), school.getZone(), school.getType());
        });
        log.info("Planned internships to assign: {}", plannedInternships.size());
        
        // Create solver for Phase 1
        SolverFactory<InternshipSolution> solverFactory = 
                SolverFactory.createFromXmlResource("solverConfig.xml");
        Solver<InternshipSolution> solver = solverFactory.buildSolver();
        
        // Prepare problem
        InternshipSolution unsolvedProblem = new InternshipSolution();
        unsolvedProblem.setAvailableTeachers(teachers);
        unsolvedProblem.setAvailableSchools(schools);
        unsolvedProblem.setPlannedInternships(plannedInternships);
        unsolvedProblem.setSchoolYear(schoolYear);
        unsolvedProblem.setTimeBudget(timeBudget);
        
        // Solve
        InternshipSolution solution = solver.solve(unsolvedProblem);
        
        log.info("Phase 1 - Best score: {}", solution.getScore());
        long assignedCount = solution.getPlannedInternships().stream()
                         .filter(i -> i.getAssignedTeacher() != null)
                         .count();
        log.info("Phase 1 - Internships with teachers: {}/{}", 
                 assignedCount, solution.getPlannedInternships().size());
        
        // Log detailed Phase 1 assignments
        log.info("=== PHASE 1 RESULTS ===");
        solution.getPlannedInternships().forEach(internship -> {
            if (internship.getAssignedTeacher() != null) {
                log.info("  ✓ {} {} [Course: {}] → Teacher {} @ School {} (Zone {})",
                        internship.getPraktikumType(),
                        internship.getSchoolType(),
                        internship.getCourse() != null ? internship.getCourse() : "N/A",
                        internship.getAssignedTeacher().getTeacherId(),
                        internship.getAssignedSchool() != null ? internship.getAssignedSchool().getId() : "NULL",
                        internship.getAssignedSchool() != null ? internship.getAssignedSchool().getZone() : "NULL");
            } else {
                log.warn("  ✗ {} {} [Course: {}] → NO TEACHER ASSIGNED",
                        internship.getPraktikumType(),
                        internship.getSchoolType(),
                        internship.getCourse() != null ? internship.getCourse() : "N/A");
            }
        });
        log.info("=======================");
        
        if (assignedCount == 0) {
            log.error("Phase 1 FAILED: No teachers assigned to any internships!");
            log.error("This means constraints are preventing all teacher assignments.");
        }
        
        return solution;
    }

    /**
     * Creates planned internship slots based on student demand.
     */
    private List<PlannedInternship> createPlannedInternshipsFromDemand(
            List<StudentConfig> studentConfigs,
            String schoolYear) {
        
        log.info("Creating planned internships from {} student configs", studentConfigs.size());
        List<PlannedInternship> internships = new ArrayList<>();
        
        for (StudentConfig config : studentConfigs) {
            log.debug("Processing student {}: pdpI={}, pdpII={}, zsp={}, sfp={}", 
                     config.getStudent().getMatriculationNbr(),
                     config.isPdpI(), config.isPdpII(), config.isZsp(), config.isSfp());
            SchoolType schoolType = config.getSchoolType();
            
            // PDP_I - null course to allow assignment to any teacher
            if (config.isPdpI()) {
                internships.add(createInternshipSlot(
                        PraktikumType.PDP_I, schoolType, null, schoolYear, PDP_CAPACITY));
            }
            
            // PDP_II - null course to allow assignment to any teacher
            if (config.isPdpII()) {
                internships.add(createInternshipSlot(
                        PraktikumType.PDP_II, schoolType, null, schoolYear, PDP_CAPACITY));
            }
            
            // ZSP - needs course matching
            if (config.isZsp()) {
                internships.add(createInternshipSlot(
                        PraktikumType.ZSP, schoolType, config.getMainCourse(), 
                        schoolYear, ZSP_CAPACITY));
            }
            
            // SFP - needs course matching
            if (config.isSfp()) {
                internships.add(createInternshipSlot(
                        PraktikumType.SFP, schoolType, config.getMainCourse(), 
                        schoolYear, SFP_CAPACITY));
            }
        }
        
        log.info("Created {} planned internship slots", internships.size());
        internships.forEach(i -> log.debug("  - {} {} {} (capacity: {})", 
                                          i.getPraktikumType(), i.getSchoolType(), 
                                          i.getCourse(), i.getMaxCapacity()));
        return internships;
    }

    /**
     * Creates a single planned internship slot.
     */
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
                .schoolYear(schoolYear)
                .maxCapacity(capacity)
                .currentAssignments(0)
                .build();
    }
}
