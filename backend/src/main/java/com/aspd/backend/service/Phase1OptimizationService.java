package com.aspd.backend.service;

import com.aspd.backend.model.*;
import com.aspd.backend.repository.CourseRepository;
import com.aspd.backend.repository.PlannedInternshipRepository;
import com.aspd.backend.solver.InternshipSolution;
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

import static com.aspd.backend.common.constants.InternshipConstants.*;

/**
 * Service for Phase 1 optimization: Assign teachers and schools to planned internships.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class Phase1OptimizationService {

    private final PlannedInternshipRepository plannedInternshipRepository;
    private final CourseRepository courseRepository;

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
        
        // Fetch all active courses for ZSP assignment
        List<Course> courses = courseRepository.findByActiveTrue();
        
        // Run Phase 1
        InternshipSolution phase1Result = runPhase1(
                teachers, schools, courses, plannedInternships, schoolYear, timeBudget, zspDistribution);
        
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
            List<Course> courses,
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
        unsolvedProblem.setAvailableCourses(courses);
        unsolvedProblem.setPlannedInternships(plannedInternships);
        unsolvedProblem.setSchoolYear(schoolYear);
        unsolvedProblem.setTimeBudget(timeBudget);
        unsolvedProblem.setBudget(new InternshipBudget(timeBudget));
        unsolvedProblem.setZspCourseDistribution(zspDistribution);
        
        // Pre-calculate minimum activation requirements (avoids groupBy caching issues)
        List<InternshipTypeRequirement> typeRequirements = buildTypeRequirements(plannedInternships);
        unsolvedProblem.setTypeRequirements(typeRequirements);
        
        // Solve
        InternshipSolution solution = solver.solve(unsolvedProblem);

        // Post-solve diagnostic: verify minimum-activation constraint status
        logMinimumActivationStatus(solution.getPlannedInternships());
        
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
     * Build type requirements from internship slots.
     * Pre-calculates totals to avoid groupBy caching issues in constraint streams.
     */
    private List<InternshipTypeRequirement> buildTypeRequirements(List<PlannedInternship> internships) {
        Map<String, InternshipTypeRequirement> requirements = new HashMap<>();
        
        for (PlannedInternship internship : internships) {
            PraktikumType type = internship.getPraktikumType();
            SchoolType schoolType = internship.getSchoolType();
            Course course = internship.getCourse();
            
            String key;
            InternshipTypeRequirement req;
            
            if (type == PraktikumType.SFP && course != null) {
                key = type + "/" + schoolType + "/" + course.getName();
                req = requirements.getOrDefault(key, InternshipTypeRequirement.builder()
                    .type(type)
                    .schoolType(schoolType)
                    .course(course)
                    .totalSlots(0)
                    .build());
            } else {
                key = type + "/" + schoolType;
                req = requirements.getOrDefault(key, InternshipTypeRequirement.builder()
                    .type(type)
                    .schoolType(schoolType)
                    .course(null)
                    .totalSlots(0)
                    .build());
            }
            
            // Increment total
            req.setTotalSlots(req.getTotalSlots() + 1);
            
            // Calculate required active
            int divisor = type == PraktikumType.PDP_I || type == PraktikumType.PDP_II ? 2 : 4;
            int required = (int) Math.ceil(req.getTotalSlots() / (double) divisor);
            req.setRequiredActive(required);
            
            requirements.put(key, req);
        }
        
        return new ArrayList<>(requirements.values());
    }}
