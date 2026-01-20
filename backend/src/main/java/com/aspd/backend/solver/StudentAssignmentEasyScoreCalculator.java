package com.aspd.backend.solver;

import com.aspd.backend.model.StudentInternshipDemand;
import com.aspd.backend.model.PlannedInternship;
import com.aspd.backend.model.PraktikumType;
import com.aspd.backend.model.Course;
import com.aspd.backend.model.StudentConfig;
import com.aspd.backend.model.Student;
import com.aspd.backend.model.Address;
import com.aspd.backend.dto.CoordinatesDto;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import org.optaplanner.core.api.score.calculator.EasyScoreCalculator;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * Phase 2 score calculator: assign students to internship slots.
 * 
 * Phase 1 guarantees (fixed):
 * - All active internships have assigned teachers and schools (immutable)
 * - Type/school-type matching is guaranteed
 * - PDP internships have null courses, SFP courses are fixed, ZSP courses assigned
 * 
 * Phase 2 optimization:
 * - Hard: Every student assigned, every internship has ≥1 student, capacity respected
 * - Soft: Minimize distance from students to their assigned school
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
            
            // Check hard constraints
            hardScore += checkHardConstraints(demand, internship);
            
            // Check soft constraints for optimization
            softScore += checkSoftConstraints(demand, internship);
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
        
        // Hard constraint: Every active internship must have at least 1 student
        for (PlannedInternship internship : solution.getInternships()) {
            if (internship.isActive()) {
                long assignedCount = assignmentCounts.getOrDefault(internship, 0L);
                if (assignedCount == 0) {
                    hardScore -= 100; // Unassigned active internship
                }
            }
        }
        
        // Soft constraint: Minimize distance from students to assigned schools
        softScore += calculateTotalDistancePreference(solution.getStudentDemands());
        
        return HardSoftScore.of(hardScore, softScore);
    }

    /**
     * Hard constraints - Phase 2 specific validations.
     */
    private int checkHardConstraints(StudentInternshipDemand demand, PlannedInternship internship) {
        int penalty = 0;
        
        // Critical constraint: Student demand type must match internship type
        if (!demand.getPraktikumType().equals(internship.getPraktikumType())) {
            penalty -= 100;
        }
        
        // Critical constraint: School type must match (GS or MS)
        if (!demand.getStudentConfig().getSchoolType().equals(internship.getSchoolType())) {
            penalty -= 100;
        }
        
        // SFP courses must match (Phase 1 guarantees this, but validate as safety check)
        if (internship.getPraktikumType() == PraktikumType.SFP && internship.getCourse() != null) {
            Course internshipCourse = internship.getCourse();
            StudentConfig config = demand.getStudentConfig();
            if (!internshipCourse.equals(config.getMainCourse())) {
                penalty -= 100;
            }
        }
        
        return penalty;
    }

    /**
     * Soft constraints for optimization.
     */
    private int checkSoftConstraints(StudentInternshipDemand demand, PlannedInternship internship) {
        int reward = 0;
        
        // For ZSP, reward matching student course preferences with internship course
        if (internship.getPraktikumType() == PraktikumType.ZSP && internship.getCourse() != null) {
            reward += evaluateZspCourseMatch(demand, internship.getCourse());
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
     * Soft constraint: minimize total distance from students to assigned schools.
     * Uses Haversine distance on actual coordinates.
     */
    private int calculateTotalDistancePreference(java.util.List<StudentInternshipDemand> demands) {
        double totalDistance = 0.0;
        
        for (StudentInternshipDemand demand : demands) {
            if (demand.getAssignedInternship() == null) {
                continue;
            }
            
            PlannedInternship internship = demand.getAssignedInternship();
            
            // Get school coordinates (Phase 1 guarantees school is assigned)
            if (internship.getSchool() == null || 
                internship.getSchool().getLatitude() == null || 
                internship.getSchool().getLongitude() == null) {
                continue;
            }
            
            // Get student coordinates (prefer semester, fallback to home)
            Student student = demand.getStudentConfig().getStudent();
            if (student == null) {
                continue;
            }
            
            Address studentAddr = null;
            if (student.getAddressSemester() != null && 
                student.getAddressSemester().getLatitude() != null && 
                student.getAddressSemester().getLongitude() != null) {
                studentAddr = student.getAddressSemester();
            } else if (student.getAddress() != null && 
                       student.getAddress().getLatitude() != null && 
                       student.getAddress().getLongitude() != null) {
                studentAddr = student.getAddress();
            }
            
            if (studentAddr == null) {
                continue; // Student has no coordinates
            }
            
            // Calculate distance using Haversine
            CoordinatesDto studentCoord = new CoordinatesDto(studentAddr.getLongitude(), studentAddr.getLatitude());
            CoordinatesDto schoolCoord = new CoordinatesDto(
                internship.getSchool().getLongitude(), 
                internship.getSchool().getLatitude()
            );
            
            Double distance = studentCoord.distanceTo(schoolCoord);
            if (distance != null) {
                totalDistance += distance;
            }
        }
        
        // Soft penalty: negative of total kilometers
        return (int) Math.round(-totalDistance);
    }
}
