package com.aspd.backend.solver;

import com.aspd.backend.model.*;
import com.aspd.backend.dto.CoordinatesDto;
import lombok.extern.slf4j.Slf4j;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import org.optaplanner.core.api.score.calculator.EasyScoreCalculator;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Manual score calculator to avoid OptaPlanner groupBy caching issues.
 * Calculates all constraints in pure Java without constraint streams.
 */
@Slf4j
@Component
public class InternshipEasyScoreCalculator implements EasyScoreCalculator<InternshipSolution, HardSoftScore> {

    @Override
    public HardSoftScore calculateScore(InternshipSolution solution) {
        int hardScore = 0;
        int softScore = 0;

        List<PlannedInternship> internships = solution.getPlannedInternships();

        // HARD CONSTRAINTS
        hardScore += calculateBudgetConstraint(solution, internships);
        hardScore += calculateMinimumActivationConstraints(internships);
        hardScore += calculateTeacherWorkloadConstraint(internships);
        hardScore += calculateTeacherDiversityConstraint(internships);
        hardScore += calculateTeacherMaxPraktikaConstraint(internships);
        hardScore += calculateTeacherPraktikumTypeConstraint(internships);
        hardScore += calculateTeacherCourseMatchConstraint(internships);
        hardScore += calculateCoursePinningConstraint(internships);
        hardScore += calculateZspCourseAssignmentConstraint(internships);
        hardScore += calculateSchoolTypeMatchConstraint(internships);
        hardScore += calculateTeacherAssignmentConstraint(internships);
        hardScore += calculateZoneConstraint(internships);

        // SOFT CONSTRAINTS
        softScore += calculateTeacher2InternshipsPreference(internships);
        softScore += calculateDiversityPreference(internships);
        softScore += calculateZspCourseDistributionPreference(solution, internships);
        softScore += calculatePdpDistancePreference(solution, internships);
        softScore += calculateZonePreference(internships);

        return HardSoftScore.of(hardScore, softScore);
    }

    /**
     * Soft constraint: minimize bidirectional + centroid distance for PDP assignments.
     * 
     * Two mechanisms:
     * 1. Bidirectional matching (local): each student finds closest teacher, each teacher finds closest student.
     *    Ensures no isolated students/teachers.
     * 2. Centroid distance (global): teachers' average position vs. student demand centroid.
     *    Ensures teachers cluster toward center of student demand.
     */
    private int calculatePdpDistancePreference(InternshipSolution solution, List<PlannedInternship> internships) {
        // Get student coords by type
        List<CoordinatesDto> gsCoords = Optional.ofNullable(solution.getPdpGsStudentCoords()).orElse(Collections.emptyList());
        List<CoordinatesDto> msCoords = Optional.ofNullable(solution.getPdpMsStudentCoords()).orElse(Collections.emptyList());

        if (gsCoords.isEmpty() && msCoords.isEmpty()) {
            return 0;
        }

        // Build teacher coords per type from active PDP assignments
        Map<SchoolType, List<CoordinatesDto>> teacherCoordsByType = new HashMap<>();
        for (PlannedInternship i : internships) {
            if (!i.isActive() || i.getAssignedTeacher() == null) {
                continue;
            }
            if (i.getPraktikumType() != PraktikumType.PDP_I && i.getPraktikumType() != PraktikumType.PDP_II) {
                continue;
            }
            Teacher t = i.getAssignedTeacher();
            School s = t.getSchool();
            if (s == null || s.getLatitude() == null || s.getLongitude() == null) {
                continue;
            }
            SchoolType type = i.getSchoolType();
            teacherCoordsByType.computeIfAbsent(type, k -> new ArrayList<>())
                .add(new CoordinatesDto(s.getLongitude(), s.getLatitude()));
        }

        double totalDistance = 0.0;

        // ===== PART 1: BIDIRECTIONAL MATCHING =====
        
        // 1a. For each student, find closest teacher (per type)
        for (SchoolType type : SchoolType.values()) {
            List<CoordinatesDto> studentCoords = (type == SchoolType.GS) ? gsCoords : msCoords;
            List<CoordinatesDto> teacherCoords = teacherCoordsByType.getOrDefault(type, Collections.emptyList());

            if (studentCoords.isEmpty() || teacherCoords.isEmpty()) {
                continue;
            }

            for (CoordinatesDto sCoord : studentCoords) {
                Double minDist = Double.MAX_VALUE;
                for (CoordinatesDto tCoord : teacherCoords) {
                    Double d = sCoord.distanceTo(tCoord);
                    if (d != null && d < minDist) {
                        minDist = d;
                    }
                }
                if (minDist != Double.MAX_VALUE) {
                    totalDistance += minDist;
                }
            }
        }

        // 1b. For each teacher, find closest student (per type)
        for (SchoolType type : SchoolType.values()) {
            List<CoordinatesDto> studentCoords = (type == SchoolType.GS) ? gsCoords : msCoords;
            List<CoordinatesDto> teacherCoords = teacherCoordsByType.getOrDefault(type, Collections.emptyList());

            if (studentCoords.isEmpty() || teacherCoords.isEmpty()) {
                continue;
            }

            for (CoordinatesDto tCoord : teacherCoords) {
                Double minDist = Double.MAX_VALUE;
                for (CoordinatesDto sCoord : studentCoords) {
                    Double d = tCoord.distanceTo(sCoord);
                    if (d != null && d < minDist) {
                        minDist = d;
                    }
                }
                if (minDist != Double.MAX_VALUE) {
                    totalDistance += minDist;
                }
            }
        }

        // ===== PART 2: CENTROID DISTANCE =====
        // Each teacher's distance to the centroid of students (per type)
        // Ensures geographic spread toward student demand center
        
        for (SchoolType type : SchoolType.values()) {
            List<CoordinatesDto> studentCoords = (type == SchoolType.GS) ? gsCoords : msCoords;
            List<CoordinatesDto> teacherCoords = teacherCoordsByType.getOrDefault(type, Collections.emptyList());

            if (studentCoords.isEmpty() || teacherCoords.isEmpty()) {
                continue;
            }

            // Compute student centroid
            double sumLat = 0.0, sumLon = 0.0;
            for (CoordinatesDto coord : studentCoords) {
                sumLat += coord.getLatitude();
                sumLon += coord.getLongitude();
            }
            double centroidLat = sumLat / studentCoords.size();
            double centroidLon = sumLon / studentCoords.size();
            CoordinatesDto centroid = new CoordinatesDto(centroidLon, centroidLat);

            // Sum distance from each teacher to centroid
            for (CoordinatesDto tCoord : teacherCoords) {
                Double d = tCoord.distanceTo(centroid);
                if (d != null) {
                    totalDistance += d;
                }
            }
        }

        // Soft penalty: negative of total kilometers
        return (int) Math.round(-totalDistance);
    }

    private int calculateBudgetConstraint(InternshipSolution solution, List<PlannedInternship> internships) {
        long activeCount = internships.stream().filter(PlannedInternship::isActive).count();
        int budget = solution.getBudget().getMaxActiveInternships();
        int deviation = (int) Math.abs(activeCount - budget);
        int score = -deviation * 2000;
        return score;
    }

    private int calculateMinimumActivationConstraints(List<PlannedInternship> internships) {
        int hardScore = 0;

        // Group by type/schoolType/course and check minimum requirements
        Map<String, Long> typeSchoolTotals = new HashMap<>();
        Map<String, Long> typeSchoolActive = new HashMap<>();

        for (PlannedInternship i : internships) {
            String key = buildConstraintKey(i);
            typeSchoolTotals.merge(key, 1L, Long::sum);
            if (i.isActive()) {
                typeSchoolActive.merge(key, 1L, Long::sum);
            }
        }

        // Check each requirement
        for (Map.Entry<String, Long> entry : typeSchoolTotals.entrySet()) {
            String key = entry.getKey();
            long total = entry.getValue();
            long active = typeSchoolActive.getOrDefault(key, 0L);

            int divisor = getDivisor(key);
            int required = (int) Math.ceil(total / (double) divisor);
            int deficit = Math.max(0, required - (int) active);

            if (deficit > 0) {
                hardScore -= deficit * 500; // Penalize each missing active slot
            }
        }

        return hardScore;
    }

    private String buildConstraintKey(PlannedInternship i) {
        if (i.getPraktikumType() == PraktikumType.SFP && i.getCourse() != null) {
            return i.getPraktikumType() + "/" + i.getSchoolType() + "/" + i.getCourse().getName();
        }
        return i.getPraktikumType() + "/" + i.getSchoolType();
    }

    private int getDivisor(String key) {
        if (key.contains("ZSP") || key.contains("SFP")) {
            return 4;
        }
        return 2; // Default for PDP
    }

    private int calculateTeacherWorkloadConstraint(List<PlannedInternship> internships) {
        int hardScore = 0;

        // Group by teacher
        Map<Teacher, Long> teacherCounts = internships.stream()
            .filter(i -> i.isActive() && i.getAssignedTeacher() != null)
            .collect(Collectors.groupingBy(PlannedInternship::getAssignedTeacher, Collectors.counting()));

        // Teachers can take 0, 1, 2, or 4 internships - not 3 or 5+
        for (long count : teacherCounts.values()) {
            if (count == 3 || count > 4) {
                hardScore -= 50; // Penalize invalid workload (teacher preference)
            }
        }

        return hardScore;
    }

    private int calculateTeacherDiversityConstraint(List<PlannedInternship> internships) {
        int hardScore = 0;

        // Group by teacher
        Map<Teacher, List<PlannedInternship>> byTeacher = internships.stream()
            .filter(i -> i.isActive() && i.getAssignedTeacher() != null)
            .collect(Collectors.groupingBy(PlannedInternship::getAssignedTeacher));

        // If teacher takes 2 or 4 internships, they must all be different types
        for (List<PlannedInternship> assignments : byTeacher.values()) {
            int count = assignments.size();
            if (count != 1) {
                long uniqueTypes = assignments.stream()
                    .map(PlannedInternship::getPraktikumType)
                    .distinct()
                    .count();
                if (uniqueTypes != count) {
                    hardScore -= 500; // Penalize if not all different
                }
            }
        }

        return hardScore;
    }

    private int calculateTeacherMaxPraktikaConstraint(List<PlannedInternship> internships) {
        int hardScore = 0;

        // Group by teacher (all internships in this run are for the same school year)
        Map<Teacher, List<PlannedInternship>> byTeacher = internships.stream()
            .filter(i -> i.isActive() && i.getAssignedTeacher() != null)
            .collect(Collectors.groupingBy(PlannedInternship::getAssignedTeacher));

        // Check each teacher's workload
        for (Map.Entry<Teacher, List<PlannedInternship>> entry : byTeacher.entrySet()) {
            Teacher teacher = entry.getKey();
            List<PlannedInternship> assignments = entry.getValue();
            
            if (assignments.isEmpty()) continue;
            
            String schoolYear = assignments.get(0).getSchoolYear();
            
            TeacherPlConfig config = teacher.getPlConfigs().stream()
                .filter(c -> c.getSchoolYear().equals(schoolYear))
                .findFirst()
                .orElse(null);

            if (config != null && config.getMaxPraktikaPerYear() != null) {
                int maxAllowed = config.getMaxPraktikaPerYear();
                int actualCount = assignments.size();
                
                if (actualCount > maxAllowed) {
                    hardScore -= (actualCount - maxAllowed) * 40; // Teacher preference per excess
                }
            }
        }

        return hardScore;
    }

    private int calculateTeacherPraktikumTypeConstraint(List<PlannedInternship> internships) {
        int hardScore = 0;

        for (PlannedInternship i : internships) {
            if (!i.isActive() || i.getAssignedTeacher() == null) {
                continue;
            }

            Teacher teacher = i.getAssignedTeacher();
            String schoolYear = i.getSchoolYear();
            PraktikumType type = i.getPraktikumType();

            TeacherPlConfig config = teacher.getPlConfigs().stream()
                .filter(c -> c.getSchoolYear().equals(schoolYear))
                .findFirst()
                .orElse(null);

            // Teacher must support the internship type
            if (config == null || !config.getInternshipPreferences().contains(type)) {
                hardScore -= 50; // Teacher preference, not eligibility
            }
        }

        return hardScore;
    }

    private int calculateTeacherCourseMatchConstraint(List<PlannedInternship> internships) {
        int hardScore = 0;

        for (PlannedInternship i : internships) {
            if (!i.isActive() || i.getAssignedTeacher() == null) {
                continue;
            }

            PraktikumType type = i.getPraktikumType();
            // ZSP and SFP require course matching (teacher must support the course)
            if ((type == PraktikumType.ZSP || type == PraktikumType.SFP) && i.getCourse() != null) {
                Teacher teacher = i.getAssignedTeacher();
                String schoolYear = i.getSchoolYear();
                Course requiredCourse = i.getCourse();

                // Check if main subject matches
                if (teacher.getMainSubject() != null && teacher.getMainSubject().equals(requiredCourse)) {
                    continue; // No violation
                }

                // Check if any specialization matches
                TeacherPlConfig config = teacher.getPlConfigs().stream()
                    .filter(c -> c.getSchoolYear().equals(schoolYear))
                    .findFirst()
                    .orElse(null);

                if (config == null || !config.getSubjectSpecializations().contains(requiredCourse)) {
                    hardScore -= 50; // Teacher preference, not eligibility
                }
            }
        }

        return hardScore;
    }

    private int calculateCoursePinningConstraint(List<PlannedInternship> internships) {
        int hardScore = 0;

        for (PlannedInternship i : internships) {
            PraktikumType type = i.getPraktikumType();
            
            // PDP_I and PDP_II must NOT have a course assigned
            if ((type == PraktikumType.PDP_I || type == PraktikumType.PDP_II) && i.getCourse() != null) {
                hardScore -= 1000; // Heavy penalty - this should never happen
            }
            
            // SFP courses must NOT be changed from their original value
            if (type == PraktikumType.SFP && i.getOriginalCourse() != null) {
                if (i.getCourse() == null || !i.getCourse().equals(i.getOriginalCourse())) {
                    hardScore -= 1000; // Heavy penalty - SFP courses are fixed
                }
            }
            
            // ZSP courses are fully flexible (planning variable)
        }

        return hardScore;
    }

    private int calculateZspCourseAssignmentConstraint(List<PlannedInternship> internships) {
        int hardScore = 0;

        for (PlannedInternship i : internships) {
            // Active ZSP internships must have a course assigned
            if (i.isActive() && i.getPraktikumType() == PraktikumType.ZSP && i.getCourse() == null) {
                hardScore -= 150; // Penalty for ZSP without course
            }
        }

        return hardScore;
    }

    private int calculateSchoolTypeMatchConstraint(List<PlannedInternship> internships) {
        int hardScore = 0;

        for (PlannedInternship i : internships) {
            if (i.isActive() && i.getSchool() != null) {
                // Penalize if school is inactive
                if (!Boolean.TRUE.equals(i.getSchool().getActive())) {
                    hardScore -= 300; // Cannot assign inactive school
                }
                // Check type match
                else if (i.getSchool().getType() != i.getSchoolType()) {
                    hardScore -= 300; // School type doesn't match requirement
                }
            }
        }

        return hardScore;
    }

    private int calculateTeacherAssignmentConstraint(List<PlannedInternship> internships) {
        int hardScore = 0;

        for (PlannedInternship i : internships) {
            if (i.isActive() && i.getAssignedTeacher() == null) {
                hardScore -= 300; // Active internship without assigned teacher
            }
            // Penalize if assigned teacher is inactive
            if (i.isActive() && i.getAssignedTeacher() != null && !i.getAssignedTeacher().isActive()) {
                hardScore -= 300; // Cannot assign inactive teacher
            }
        }

        return hardScore;
    }

    /**
     * HARD constraint: Zone feasibility based on internship type and ÖPNV availability.
     * 
     * ZSP & SFP (students go to schools):
     * - Zone 1: Feasible
     * - Zone 2: Feasible (if necessary, ÖPNV doesn't matter)
     * - Zone 3+: NOT feasible
     * 
     * PDP_I & PDP_II (teachers go to schools):
     * - Zone 3: Feasible
     * - Zone 2: Feasible (if necessary)
     * - Zone 1: NOT feasible
     * - ÖPNV 4b: NOT compatible with PDP (1 hour travel too long for teachers)
     */
    private int calculateZoneConstraint(List<PlannedInternship> internships) {
        int hardScore = 0;

        for (PlannedInternship i : internships) {
            if (!i.isActive() || i.getSchool() == null) {
                continue;
            }

            PraktikumType type = i.getPraktikumType();
            String zone = i.getSchool().getZone();
            OepnvStatus oepnv = i.getSchool().getOepnv();

            boolean isViolation = false;
            if (type == PraktikumType.ZSP || type == PraktikumType.SFP) {
                // ZSP & SFP: Zone 1 or Zone 2 acceptable (ÖPNV doesn't matter)
                if ("1".equals(zone) || "2".equals(zone)) {
                    isViolation = false;
                }
                // Zone 3+ is NOT OK
                else {
                    isViolation = true;
                }
            } else { // PDP_I or PDP_II
                // CRITICAL: PDP cannot use schools with ÖPNV 4b (1 hour travel too long)
                if (oepnv == OepnvStatus.FOUR_B) {
                    isViolation = true;
                }
                // PDP: Zone 2 or Zone 3 acceptable
                else if ("2".equals(zone) || "3".equals(zone)) {
                    isViolation = false;
                }
                // Zone 1 is NOT OK for PDP
                else {
                    isViolation = true;
                }
            }

            if (isViolation) {
                hardScore -= 200;
            }
        }

        return hardScore;
    }

    /**
     * SOFT constraint: Prefer optimal zones.
     * 
     * ZSP & SFP prefer Zone 1:
     * - Zone 1: Preferred (bonus)
     * - Zone 2: Acceptable but not preferred (penalty)
     * 
     * PDP_I & PDP_II prefer Zone 3:
     * - Zone 3: Preferred (bonus)
     * - Zone 2: Acceptable but not preferred (penalty)
     */
    private int calculateZonePreference(List<PlannedInternship> internships) {
        int softScore = 0;

        for (PlannedInternship i : internships) {
            if (!i.isActive() || i.getSchool() == null) {
                continue;
            }

            PraktikumType type = i.getPraktikumType();
            String zone = i.getSchool().getZone();

            if (type == PraktikumType.ZSP || type == PraktikumType.SFP) {
                // Prefer Zone 1 for ZSP & SFP
                if ("1".equals(zone)) {
                    softScore += 10; // Bonus for preferred zone
                } else if ("2".equals(zone)) {
                    softScore -= 5; // Penalty for "if necessary" zone
                }
            } else if (type == PraktikumType.PDP_I || type == PraktikumType.PDP_II) {
                // Prefer Zone 3 for PDP
                if ("3".equals(zone)) {
                    softScore += 10; // Bonus for preferred zone
                } else if ("2".equals(zone)) {
                    softScore -= 5; // Penalty for "if necessary" zone
                }
            }
        }

        return softScore;
    }

    private int calculateTeacher2InternshipsPreference(List<PlannedInternship> internships) {
        int softScore = 0;

        Map<Teacher, Long> teacherCounts = internships.stream()
            .filter(i -> i.isActive() && i.getAssignedTeacher() != null)
            .collect(Collectors.groupingBy(PlannedInternship::getAssignedTeacher, Collectors.counting()));

        // Reward teachers with exactly 2 internships
        for (long count : teacherCounts.values()) {
            if (count == 2) {
                softScore += 20; // Bonus points for ideal workload
            }
        }

        return softScore;
    }

    /**
     * SOFT constraint: Prefer diverse internship types at each school.
     * Penalizes schools where one type dominates (lack of diversity).
     * 
     * Calculates: most_common_type_count / total_internships_at_school
     * - Lower ratio = more diverse = better (bonus)
     * - Higher ratio = less diverse = worse (penalty)
     * 
     * Examples:
     * - School with 3 PDP_I, 1 ZSP: ratio = 3/4 = 0.75 (not diverse, penalty)
     * - School with 2 PDP_I, 2 ZSP: ratio = 2/4 = 0.50 (better diversity)
     * - School with 1 of each type: ratio = 1/4 = 0.25 (most diverse, bonus)
     */
    private int calculateDiversityPreference(List<PlannedInternship> internships) {
        int softScore = 0;

        // Group internships by school
        Map<Long, List<PlannedInternship>> bySchool = new HashMap<>();
        for (PlannedInternship i : internships) {
            if (i.isActive() && i.getSchool() != null) {
                bySchool.computeIfAbsent(i.getSchool().getId(), k -> new ArrayList<>()).add(i);
            }
        }

        // For each school, calculate diversity ratio
        for (List<PlannedInternship> schoolInternships : bySchool.values()) {
            if (schoolInternships.size() <= 1) {
                continue; // Single internship = perfectly diverse by default
            }

            // Count each type at this school
            Map<PraktikumType, Long> typeCounts = schoolInternships.stream()
                .collect(Collectors.groupingBy(PlannedInternship::getPraktikumType, Collectors.counting()));

            // Find the most common type count
            long maxCount = typeCounts.values().stream().max(Long::compare).orElse(0L);
            int totalCount = schoolInternships.size();

            // Calculate diversity ratio (0.0 to 1.0)
            double ratio = (double) maxCount / totalCount;

            // Penalize high ratios (lack of diversity)
            // Ratio of 1.0 (all same type) = -10 points
            // Ratio of 0.5 (balanced) = -5 points
            // Ratio of 0.25 (most diverse) = -2.5 points
            softScore -= (int) Math.round(ratio * 10);
        }

        return softScore;
    }

    private int calculateZspCourseDistributionPreference(InternshipSolution solution, List<PlannedInternship> internships) {
        int softScore = 0;

        ZspCourseDistribution distribution = solution.getZspCourseDistribution();
        if (distribution == null) {
            return softScore;
        }

        // Group ZSP internships by school type and teacher's main subject
        Map<String, Long> zspByCourse = new HashMap<>();
        
        for (PlannedInternship i : internships) {
            if (i.isActive() && i.getPraktikumType() == PraktikumType.ZSP && i.getAssignedTeacher() != null) {
                Course mainSubject = i.getAssignedTeacher().getMainSubject();
                if (mainSubject != null) {
                    String key = i.getSchoolType() + "/" + mainSubject.getId();
                    zspByCourse.merge(key, 1L, Long::sum);
                }
            }
        }

        // Compare with preferred distribution
        for (Map.Entry<String, Long> entry : zspByCourse.entrySet()) {
            String[] parts = entry.getKey().split("/");
            if (parts.length != 2) continue;
            
            SchoolType schoolType = SchoolType.valueOf(parts[0]);
            Long courseId = Long.parseLong(parts[1]);
            long actualCount = entry.getValue();

            // Get desired weight from distribution
            Map<Course, Double> courseMap = (schoolType == SchoolType.GS) 
                ? distribution.getGsDistribution() 
                : distribution.getMsDistribution();

            double desiredWeight = 0.0;
            for (Map.Entry<Course, Double> courseEntry : courseMap.entrySet()) {
                if (courseEntry.getKey().getId().equals(courseId)) {
                    desiredWeight = courseEntry.getValue();
                    break;
                }
            }

            // Calculate distance and apply penalty
            double distance = Math.abs(actualCount - desiredWeight);
            softScore -= (int) Math.round(distance * 10);
        }

        return softScore;
    }

    private boolean hasOepnv(OepnvStatus status) {
        return status != null && status.isAvailable();
    }
}
