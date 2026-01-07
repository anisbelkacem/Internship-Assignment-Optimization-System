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
            } else {
                // ASSIGNED: +100 soft points (reward)
                softScore += 100;
                
                PlannedInternship internship = demand.getAssignedInternship();
                
                // Check hard constraints
                // 1. Type must match
                if (!demand.getPraktikumType().equals(internship.getPraktikumType())) {
                    hardScore -= 50;
                }
                
                // 2. School type must match
                if (demand.getStudentSchoolType() != null && 
                    !demand.getStudentSchoolType().equals(internship.getSchoolType())) {
                    hardScore -= 50;
                }
                
                // 3. For ZSP/SFP, must have teacher and school
                PraktikumType type = demand.getPraktikumType();
                if (type == PraktikumType.ZSP || type == PraktikumType.SFP) {
                    if (internship.getAssignedTeacher() == null || internship.getAssignedSchool() == null) {
                        hardScore -= 100;
                    }
                }
                
                // 4. For PDP, course must be OTHER
                if (type == PraktikumType.PDP_I || type == PraktikumType.PDP_II) {
                    if (internship.getCourse() != null) {
                        hardScore -= 100;
                    }
                }
                
                // 5. For ZSP/SFP, student course must match internship course
                if (type == PraktikumType.ZSP || type == PraktikumType.SFP) {
                    if (internship.getCourse() != null && internship.getCourse() != null) {
                        Course internshipCourse = internship.getCourse();
                        StudentConfig config = demand.getStudentConfig();
                        boolean matches = internshipCourse.equals(config.getMainCourse()) ||
                                         internshipCourse.equals(config.getPrefCourse1()) ||
                                         internshipCourse.equals(config.getPrefCourse2()) ||
                                         internshipCourse.equals(config.getPrefCourse3());
                        if (!matches) {
                            hardScore -= 30;
                        } else {
                            // SOFT: Reward matching preferred courses
                            if (internshipCourse.equals(config.getMainCourse())) softScore += 10;
                            else if (internshipCourse.equals(config.getPrefCourse1())) softScore += 7;
                            else if (internshipCourse.equals(config.getPrefCourse2())) softScore += 4;
                            else if (internshipCourse.equals(config.getPrefCourse3())) softScore += 2;
                        }
                    }
                }
                
                // SOFT: Distance penalty for PDP (minimize student-to-school distance)
                if (type == PraktikumType.PDP_I || type == PraktikumType.PDP_II) {
                    if (demand.getStudentAddress() != null && internship.getAssignedSchool() != null) {
                        String zone = internship.getAssignedSchool().getZone();
                        if ("3".equals(zone)) softScore -= 30;
                        else if ("2".equals(zone)) softScore -= 15;
                        else if ("1".equals(zone)) softScore -= 5;
                    }
                }
            }
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
}
