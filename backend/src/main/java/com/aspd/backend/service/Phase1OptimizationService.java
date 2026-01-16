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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        
        log.info("Created {} internship slots\n", plannedInternships.size());
        
        // Run Phase 1
        InternshipSolution phase1Result = runPhase1(
                teachers, schools, plannedInternships, schoolYear, timeBudget, zspDistribution);
        
        // Remove inactive internships (solver decided we don't need them)
        // List<PlannedInternship> activeInternships = phase1Result.getPlannedInternships().stream()
        //         .filter(PlannedInternship::isActive)
        //         .toList();
        
        // Save only active internships to database
        // List<PlannedInternship> savedInternships = plannedInternshipRepository.saveAll(activeInternships);
        List<PlannedInternship> savedInternships = plannedInternshipRepository.saveAll(phase1Result.getPlannedInternships());
        
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
     * PHASE 1: Activate internship slots and assign teachers/schools.
     */
    private InternshipSolution runPhase1(
            List<Teacher> teachers,
            List<School> schools,
            List<PlannedInternship> plannedInternships,
            String schoolYear,
            Integer timeBudget,
            ZspCourseDistribution zspDistribution) {
        
        // Create solver for Phase 1
        SolverFactory<InternshipSolution> solverFactory = 
                SolverFactory.createFromXmlResource("solverConfig.xml");
        Solver<InternshipSolution> solver = solverFactory.buildSolver();
        
        // Prepare problem
        InternshipSolution unsolvedProblem = new InternshipSolution();
        unsolvedProblem.setAvailableTeachers(teachers);
        unsolvedProblem.setPlannedInternships(plannedInternships);
        unsolvedProblem.setSchoolYear(schoolYear);
        unsolvedProblem.setTimeBudget(timeBudget);
        unsolvedProblem.setBudget(new InternshipBudget(timeBudget));
        unsolvedProblem.setZspCourseDistribution(zspDistribution);
        
        // Solve
        InternshipSolution solution = solver.solve(unsolvedProblem);
        
        return solution;
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
            .schoolYear(schoolYear)
            .maxCapacity(capacity)
            .currentAssignments(0)
            .active(Boolean.FALSE)
            .assignedTeacher(null)
            .build();
    }
}
