package com.aspd.backend.solver;

import com.aspd.backend.model.StudentInternshipDemand;
import com.aspd.backend.model.PlannedInternship;
import com.aspd.backend.model.PraktikumType;
import com.aspd.backend.model.Course;
import com.aspd.backend.model.StudentConfig;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import org.optaplanner.core.api.score.calculator.EasyScoreCalculator;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * Simplified Phase 2 score calculator leveraging Phase 1 guarantees.
 * 
 * Phase 1 guarantees:
 * - All active internships have assigned teachers and schools
 * - Type/school-type matching is guaranteed by problem structure
 * - PDP internships have null courses, SFP courses are fixed, ZSP courses assigned
 * 
 * Phase 2 focus:
 * - Hard: Capacity constraints, all students assigned, SFP course match
 * - Soft: ZSP course preferences, capacity balance, distance for PDP
 */
public class StudentAssignmentEasyScoreCalculator implements EasyScoreCalculator<StudentAssignmentSolution, HardSoftScore> {

    @Override
    public HardSoftScore calculateScore(StudentAssignmentSolution solution) {
        int hardScore = 0;
        int softScore = 0;
        
        // Count assignments and calculate scores
        for (StudentInternshipDemand demand : solution.getStudentDemands()) {
            if (demand.getAssignedInternship() == null) {
                // HARD: All students must be assigned
                hardScore -= 100;
                continue;
            }
            
            PlannedInternship internship = demand.getAssignedInternship();
            PraktikumType type = demand.getPraktikumType();
            
            // Check hard constraints (very few, mostly delegated to Phase 1)
            hardScore += checkHardConstraints(demand, internship, type);
            
            // Check soft constraints for optimization
            softScore += checkSoftConstraints(demand, internship, type);
        }
        
        // Hard constraint: Capacity must not be exceeded
        Map<PlannedInternship, Long> assignmentCounts = solution.getStudentDemands().stream()
                .filter(d -> d.getAssignedInternship() != null)
                .collect(Collectors.groupingBy(
                        StudentInternshipDemand::getAssignedInternship,
                        Collectors.counting()
                ));
        
        for (Map.Entry<PlannedInternship, Long> entry : assignmentCounts.entrySet()) {
            PlannedInternship internship = entry.getKey();
            long count = entry.getValue();
            
            if (count > internship.getMaxCapacity()) {
                hardScore -= (int) (count - internship.getMaxCapacity()) * 10;
            }
        }
        
        return HardSoftScore.of(hardScore, softScore);
    }

    /**
     * Minimal hard constraints - Phase 1 guarantees most of these already.
     * Only check SFP course matching as a safety validation.
     */
    private int checkHardConstraints(StudentInternshipDemand demand, PlannedInternship internship, PraktikumType type) {
        int penalty = 0;
        
        // Only remaining hard constraint: SFP courses must match
        // (Phase 1 guarantees this, but we validate as safety check)
        if (type == PraktikumType.SFP && internship.getCourse() != null) {
            Course internshipCourse = internship.getCourse();
            StudentConfig config = demand.getStudentConfig();
            if (!internshipCourse.equals(config.getMainCourse())) {
                penalty -= 100;
            }
        }
        
        return penalty;
    }

    /**
     * Soft constraints for optimization:
     * - ZSP course preferences (soft)
     * - Capacity balance (soft)
     * - Distance preference for PDP (soft)
     */
    private int checkSoftConstraints(StudentInternshipDemand demand, PlannedInternship internship, PraktikumType type) {
        int reward = 0;
        
        // For ZSP, reward matching student course preferences with internship course
        if (type == PraktikumType.ZSP && internship.getCourse() != null) {
            reward += evaluateZspCourseMatch(demand, internship.getCourse());
        }
        
        // For PDP, prefer closer schools (zone-based distance penalty)
        if ((type == PraktikumType.PDP_I || type == PraktikumType.PDP_II) && internship.getSchool() != null) {
            reward += evaluateDistancePreference(demand, internship);
        }
        
        return reward;
    }

    /**
     * Evaluates ZSP course match based on student preferences.
     * Rewards: +10 main course, +7 pref1, +4 pref2, +2 pref3, -30 no match
     */
    private int evaluateZspCourseMatch(StudentInternshipDemand demand, Course internshipCourse) {
        StudentConfig config = demand.getStudentConfig();
        Course mainCourse = config.getMainCourse();
        Course prefCourse1 = config.getPrefCourse1();
        Course prefCourse2 = config.getPrefCourse2();
        Course prefCourse3 = config.getPrefCourse3();
        
        if (internshipCourse.equals(mainCourse)) {
            return 10;
        }
        if (internshipCourse.equals(prefCourse1)) {
            return 7;
        }
        if (internshipCourse.equals(prefCourse2)) {
            return 4;
        }
        if (internshipCourse.equals(prefCourse3)) {
            return 2;
        }
        
        // No match found
        return -30;
    }

    /**
     * Evaluates distance preference for PDP internships.
     * Penalizes farther zones: zone 3 (-30), zone 2 (-15), zone 1 (-5)
     */
    private int evaluateDistancePreference(StudentInternshipDemand demand, PlannedInternship internship) {
        String zone = internship.getSchool().getZone();
        
        if ("3".equals(zone)) {
            return -30;
        }
        if ("2".equals(zone)) {
            return -15;
        }
        if ("1".equals(zone)) {
            return -5;
        }
        
        return 0;
    }
}
