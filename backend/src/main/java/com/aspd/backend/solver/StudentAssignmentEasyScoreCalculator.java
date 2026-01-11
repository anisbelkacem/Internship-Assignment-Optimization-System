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
 * Score calculator with reward+penalty system:
 * - Assigned student: +100 soft points (reward)
 * - Unassigned student: -100 hard points (penalty)
 * - Constraint violations: Various hard/soft penalties
 */
public class StudentAssignmentEasyScoreCalculator implements EasyScoreCalculator<StudentAssignmentSolution, HardSoftScore> {

    @Override
    public HardSoftScore calculateScore(StudentAssignmentSolution solution) {
        int hardScore = 0;
        int softScore = 0;
        
        // Count assignments and calculate scores
        for (StudentInternshipDemand demand : solution.getStudentDemands()) {
            if (demand.getAssignedInternship() == null) {
                // UNASSIGNED: -100 hard points
                hardScore -= 100;
                continue;
            }
            
            // ASSIGNED: +100 soft points (reward)
            softScore += 100;
            
            PlannedInternship internship = demand.getAssignedInternship();
            PraktikumType type = demand.getPraktikumType();
            
            // Check hard constraints
            hardScore += checkHardConstraints(demand, internship, type);
            
            // Check soft constraints
            softScore += checkSoftConstraints(demand, internship, type);
        }
        
        // Check capacity constraints
        Map<PlannedInternship, Long> assignmentCounts = solution.getStudentDemands().stream()
                .filter(d -> d.getAssignedInternship() != null)
                .collect(Collectors.groupingBy(
                        StudentInternshipDemand::getAssignedInternship,
                        Collectors.counting()
                ));
        
        for (Map.Entry<PlannedInternship, Long> entry : assignmentCounts.entrySet()) {
            PlannedInternship internship = entry.getKey();
            long count = entry.getValue();
            
            // HARD: Capacity must not be exceeded
            if (count > internship.getMaxCapacity()) {
                hardScore -= (count - internship.getMaxCapacity()) * 10;
            }
            
            // SOFT: Prefer fewer students per internship
            if (count > 1) {
                softScore -= (count - 1) * 3;
            }
        }
        
        return HardSoftScore.of(hardScore, softScore);
    }

    private int checkHardConstraints(StudentInternshipDemand demand, PlannedInternship internship, PraktikumType type) {
        int penalty = 0;
        
        // 1. Type must match
        if (!demand.getPraktikumType().equals(internship.getPraktikumType())) {
            penalty -= 50;
        }
        
        // 2. School type must match
        if (demand.getStudentSchoolType() != null && 
            !demand.getStudentSchoolType().equals(internship.getSchoolType())) {
            penalty -= 50;
        }
        
        // 3. For ZSP/SFP, must have teacher and school
        if (type == PraktikumType.ZSP || type == PraktikumType.SFP) {
            if (internship.getAssignedTeacher() == null || internship.getAssignedSchool() == null) {
                penalty -= 100;
            }
        }
        
        // 4. For PDP, course must be null (no course restriction)
        if (type == PraktikumType.PDP_I || type == PraktikumType.PDP_II) {
            if (internship.getCourse() != null) {
                penalty -= 100;
            }
        }
        
        // 5. For SFP, student course must match internship course
        if (type == PraktikumType.SFP && internship.getCourse() != null) {
            Course internshipCourse = internship.getCourse();
            StudentConfig config = demand.getStudentConfig();
            if (!internshipCourse.equals(config.getMainCourse())) {
                penalty -= 100;
            }
        }
        
        return penalty;
    }

    private int checkSoftConstraints(StudentInternshipDemand demand, PlannedInternship internship, PraktikumType type) {
        int reward = 0;
        
        // For ZSP, student course must match internship course
        if (type == PraktikumType.ZSP && internship.getCourse() != null) {
            reward += evaluateZspCourseMatch(demand, internship.getCourse());
        }
        
        // Distance penalty for PDP (minimize student-to-school distance)
        if (type == PraktikumType.PDP_I || type == PraktikumType.PDP_II) {
            reward += evaluateDistancePenalty(demand, internship);
        }
        
        return reward;
    }

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

    private int evaluateDistancePenalty(StudentInternshipDemand demand, PlannedInternship internship) {
        if (demand.getStudentAddress() == null || internship.getAssignedSchool() == null) {
            return 0;
        }
        
        String zone = internship.getAssignedSchool().getZone();
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
