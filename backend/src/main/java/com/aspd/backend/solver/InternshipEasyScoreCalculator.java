package com.aspd.backend.solver;

import com.aspd.backend.model.*;
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

    private static final boolean ENABLE_DIAGNOSTICS = false; // Disabled during iterations
    private Map<String, Integer> constraintScores = new LinkedHashMap<>();

    @Override
    public HardSoftScore calculateScore(InternshipSolution solution) {
        int hardScore = 0;
        int softScore = 0;

        if (ENABLE_DIAGNOSTICS) {
            constraintScores.clear();
        }

        List<PlannedInternship> internships = solution.getPlannedInternships();

        // HARD CONSTRAINTS
        hardScore += calculateBudgetConstraint(solution, internships);
        hardScore += calculateMinimumActivationConstraints(internships);
        hardScore += calculateTeacherWorkloadConstraint(internships);
        hardScore += calculateTeacherDiversityConstraint(internships);
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

        if (ENABLE_DIAGNOSTICS) {
            logScoreDiagnostics(hardScore, softScore);
        }

        return HardSoftScore.of(hardScore, softScore);
    }

    private void logScoreDiagnostics(int hardScore, int softScore) {
        log.info("\n========== SCORE DIAGNOSTICS ==========");
        log.info("Total Score: {} hard / {} soft\n", hardScore, softScore);
        
        log.info("HARD CONSTRAINTS:");
        for (Map.Entry<String, Integer> entry : constraintScores.entrySet()) {
            if (entry.getKey().startsWith("HARD:")) {
                log.info("  {}: {}", entry.getKey().substring(5), entry.getValue());
            }
        }
        
        log.info("\nSOFT CONSTRAINTS:");
        for (Map.Entry<String, Integer> entry : constraintScores.entrySet()) {
            if (entry.getKey().startsWith("SOFT:")) {
                log.info("  {}: {}", entry.getKey().substring(5), entry.getValue());
            }
        }
        log.info("=====================================\n");
    }

    private void recordConstraint(String name, int score) {
        if (ENABLE_DIAGNOSTICS) {
            constraintScores.put(name, score);
        }
    }

    /**
     * Manually log diagnostics for a final solution (call this after solving completes).
     */
    public void logFinalScoreDiagnostics(InternshipSolution solution) {
        constraintScores.clear();
        
        // Temporarily enable diagnostics
        int hardScore = 0;
        int softScore = 0;
        List<PlannedInternship> internships = solution.getPlannedInternships();

        // Calculate all constraints with diagnostics enabled
        hardScore += calculateBudgetConstraintWithDiagnostics(solution, internships);
        hardScore += calculateMinimumActivationConstraintsWithDiagnostics(internships);
        hardScore += calculateTeacherWorkloadConstraintWithDiagnostics(internships);
        hardScore += calculateTeacherDiversityConstraintWithDiagnostics(internships);
        hardScore += calculateTeacherPraktikumTypeConstraintWithDiagnostics(internships);
        hardScore += calculateTeacherCourseMatchConstraintWithDiagnostics(internships);
        hardScore += calculateCoursePinningConstraintWithDiagnostics(internships);
        hardScore += calculateZspCourseAssignmentConstraintWithDiagnostics(internships);
        hardScore += calculateSchoolTypeMatchConstraintWithDiagnostics(internships);
        hardScore += calculateTeacherAssignmentConstraintWithDiagnostics(internships);
        hardScore += calculateZoneConstraintWithDiagnostics(internships);

        softScore += calculateTeacher2InternshipsPreferenceWithDiagnostics(internships);
        softScore += calculateDiversityPreferenceWithDiagnostics(internships);
        softScore += calculateZspCourseDistributionPreferenceWithDiagnostics(solution, internships);

        logScoreDiagnostics(hardScore, softScore);
    }

    // Diagnostic versions of constraint methods
    private int calculateBudgetConstraintWithDiagnostics(InternshipSolution solution, List<PlannedInternship> internships) {
        long activeCount = internships.stream().filter(PlannedInternship::isActive).count();
        int budget = solution.getBudget().getMaxActiveInternships();
        int deviation = (int) Math.abs(activeCount - budget);
        int score = -deviation * 1000;
        constraintScores.put("HARD: Budget (active=" + activeCount + ", budget=" + budget + ", deviation=" + deviation + ")", score);
        return score;
    }

    private int calculateMinimumActivationConstraintsWithDiagnostics(List<PlannedInternship> internships) {
        int hardScore = 0;
        Map<String, Long> typeSchoolTotals = new HashMap<>();
        Map<String, Long> typeSchoolActive = new HashMap<>();

        for (PlannedInternship i : internships) {
            String key = buildConstraintKey(i);
            typeSchoolTotals.merge(key, 1L, Long::sum);
            if (i.isActive()) {
                typeSchoolActive.merge(key, 1L, Long::sum);
            }
        }

        for (Map.Entry<String, Long> entry : typeSchoolTotals.entrySet()) {
            String key = entry.getKey();
            long total = entry.getValue();
            long active = typeSchoolActive.getOrDefault(key, 0L);
            int divisor = getDivisor(key);
            int required = (int) Math.ceil(total / (double) divisor);
            int deficit = Math.max(0, required - (int) active);
            if (deficit > 0) {
                hardScore -= deficit * 200;
            }
        }

        constraintScores.put("HARD: Minimum Activation Requirements", hardScore);
        return hardScore;
    }

    private int calculateTeacherWorkloadConstraintWithDiagnostics(List<PlannedInternship> internships) {
        int hardScore = 0;
        Map<Teacher, Long> teacherCounts = internships.stream()
            .filter(i -> i.isActive() && i.getAssignedTeacher() != null)
            .collect(Collectors.groupingBy(PlannedInternship::getAssignedTeacher, Collectors.counting()));
        int violations = 0;
        for (long count : teacherCounts.values()) {
            if (count == 3 || count > 4) {
                hardScore -= count;
                violations++;
            }
        }
        constraintScores.put("HARD: Teacher Workload (" + violations + " teachers with invalid load)", hardScore);
        return hardScore;
    }

    private int calculateTeacherDiversityConstraintWithDiagnostics(List<PlannedInternship> internships) {
        int hardScore = 0;
        Map<Teacher, List<PlannedInternship>> byTeacher = internships.stream()
            .filter(i -> i.isActive() && i.getAssignedTeacher() != null)
            .collect(Collectors.groupingBy(PlannedInternship::getAssignedTeacher));
        for (List<PlannedInternship> assignments : byTeacher.values()) {
            int count = assignments.size();
            if (count == 2 || count == 4) {
                long uniqueTypes = assignments.stream()
                    .map(PlannedInternship::getPraktikumType)
                    .distinct()
                    .count();
                if (uniqueTypes != count) {
                    hardScore -= 20;
                }
            }
        }
        constraintScores.put("HARD: Teacher Diversity (types must differ)", hardScore);
        return hardScore;
    }

    private int calculateTeacherPraktikumTypeConstraintWithDiagnostics(List<PlannedInternship> internships) {
        int hardScore = 0;
        for (PlannedInternship i : internships) {
            if (!i.isActive() || i.getAssignedTeacher() == null) continue;
            Teacher teacher = i.getAssignedTeacher();
            String schoolYear = i.getSchoolYear();
            PraktikumType type = i.getPraktikumType();
            TeacherPlConfig config = teacher.getPlConfigs().stream()
                .filter(c -> c.getSchoolYear().equals(schoolYear))
                .findFirst().orElse(null);
            if (config == null || !config.getInternshipPreferences().contains(type)) {
                hardScore -= 1;
            }
        }
        constraintScores.put("HARD: Teacher Praktikum Type Support", hardScore);
        return hardScore;
    }

    private int calculateTeacherCourseMatchConstraintWithDiagnostics(List<PlannedInternship> internships) {
        int hardScore = 0;
        for (PlannedInternship i : internships) {
            if (!i.isActive() || i.getAssignedTeacher() == null) continue;
            PraktikumType type = i.getPraktikumType();
            if ((type == PraktikumType.ZSP || type == PraktikumType.SFP) && i.getCourse() != null) {
                Teacher teacher = i.getAssignedTeacher();
                String schoolYear = i.getSchoolYear();
                Course requiredCourse = i.getCourse();
                if (teacher.getMainSubject() != null && teacher.getMainSubject().equals(requiredCourse)) {
                    continue;
                }
                TeacherPlConfig config = teacher.getPlConfigs().stream()
                    .filter(c -> c.getSchoolYear().equals(schoolYear))
                    .findFirst().orElse(null);
                if (config == null || !config.getSubjectSpecializations().contains(requiredCourse)) {
                    hardScore -= 1;
                }
            }
        }
        constraintScores.put("HARD: Teacher Course Match (ZSP/SFP)", hardScore);
        return hardScore;
    }

    private int calculateCoursePinningConstraintWithDiagnostics(List<PlannedInternship> internships) {
        int hardScore = 0;
        for (PlannedInternship i : internships) {
            PraktikumType type = i.getPraktikumType();
            if ((type == PraktikumType.PDP_I || type == PraktikumType.PDP_II) && i.getCourse() != null) {
                hardScore -= 1000;
            }
            if (type == PraktikumType.SFP && i.getOriginalCourse() != null) {
                if (i.getCourse() == null || !i.getCourse().equals(i.getOriginalCourse())) {
                    hardScore -= 1000;
                }
            }
        }
        constraintScores.put("HARD: Course Pinning (PDP null, SFP fixed)", hardScore);
        return hardScore;
    }

    private int calculateZspCourseAssignmentConstraintWithDiagnostics(List<PlannedInternship> internships) {
        int hardScore = 0;
        int violations = 0;
        for (PlannedInternship i : internships) {
            if (i.isActive() && i.getPraktikumType() == PraktikumType.ZSP && i.getCourse() == null) {
                hardScore -= 50;
                violations++;
            }
        }
        constraintScores.put("HARD: ZSP Course Assignment (" + violations + " ZSP without course)", hardScore);
        return hardScore;
    }

    private int calculateSchoolTypeMatchConstraintWithDiagnostics(List<PlannedInternship> internships) {
        int hardScore = 0;
        int violations = 0;
        for (PlannedInternship i : internships) {
            if (i.isActive() && i.getSchool() != null && i.getSchool().getType() != i.getSchoolType()) {
                hardScore -= 100;
                violations++;
            }
        }
        constraintScores.put("HARD: School Type Match (" + violations + " mismatches)", hardScore);
        return hardScore;
    }

    private int calculateTeacherAssignmentConstraintWithDiagnostics(List<PlannedInternship> internships) {
        int hardScore = 0;
        int violations = 0;
        for (PlannedInternship i : internships) {
            if (i.isActive() && i.getAssignedTeacher() == null) {
                hardScore -= 100;
                violations++;
            }
        }
        constraintScores.put("HARD: Teacher Assignment (" + violations + " active without teacher)", hardScore);
        return hardScore;
    }

    private int calculateZoneConstraintWithDiagnostics(List<PlannedInternship> internships) {
        int hardScore = 0;
        for (PlannedInternship i : internships) {
            if (!i.isActive() || i.getSchool() == null) continue;
            PraktikumType type = i.getPraktikumType();
            String zone = i.getSchool().getZone();
            Boolean hasOepnv = i.getSchool().getOepnv();
            boolean isViolation = false;
            if (type == PraktikumType.ZSP || type == PraktikumType.SFP) {
                if ("1".equals(zone)) {
                    isViolation = false;
                } else if ("2".equals(zone) && Boolean.TRUE.equals(hasOepnv)) {
                    isViolation = false;
                } else {
                    isViolation = true;
                }
            } else {
                if ("2".equals(zone) || "3".equals(zone)) {
                    isViolation = false;
                } else {
                    isViolation = true;
                }
            }
            if (isViolation) {
                hardScore -= 1;
            }
        }
        constraintScores.put("HARD: Zone Rules (ZSP/SFP zone 1/2+OEPNV, PDP zone 2/3)", hardScore);
        return hardScore;
    }

    private int calculateTeacher2InternshipsPreferenceWithDiagnostics(List<PlannedInternship> internships) {
        int softScore = 0;
        Map<Teacher, Long> teacherCounts = internships.stream()
            .filter(i -> i.isActive() && i.getAssignedTeacher() != null)
            .collect(Collectors.groupingBy(PlannedInternship::getAssignedTeacher, Collectors.counting()));
        int teachersWith2 = 0;
        for (long count : teacherCounts.values()) {
            if (count == 2) {
                softScore += 10;
                teachersWith2++;
            }
        }
        constraintScores.put("SOFT: Teacher 2 Internships Preference (" + teachersWith2 + " teachers)", softScore);
        return softScore;
    }

    private int calculateDiversityPreferenceWithDiagnostics(List<PlannedInternship> internships) {
        int softScore = 0;
        Map<String, Long> typeBySchool = new HashMap<>();
        for (PlannedInternship i : internships) {
            if (i.isActive() && i.getSchool() != null) {
                String key = i.getSchool().getId() + "/" + i.getPraktikumType();
                typeBySchool.merge(key, 1L, Long::sum);
            }
        }
        int repetitions = 0;
        for (long count : typeBySchool.values()) {
            if (count > 1) {
                softScore -= (count - 1) * 2;
                repetitions += (count - 1);
            }
        }
        constraintScores.put("SOFT: Type Diversity Per School (" + repetitions + " repetitions)", softScore);
        return softScore;
    }

    private int calculateZspCourseDistributionPreferenceWithDiagnostics(InternshipSolution solution, List<PlannedInternship> internships) {
        int softScore = 0;
        ZspCourseDistribution distribution = solution.getZspCourseDistribution();
        if (distribution == null) {
            constraintScores.put("SOFT: ZSP Course Distribution", softScore);
            return 0;
        }
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
        for (Map.Entry<String, Long> entry : zspByCourse.entrySet()) {
            String[] parts = entry.getKey().split("/");
            if (parts.length != 2) continue;
            SchoolType schoolType = SchoolType.valueOf(parts[0]);
            Long courseId = Long.parseLong(parts[1]);
            long actualCount = entry.getValue();
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
            double distance = Math.abs(actualCount - desiredWeight);
            softScore -= (int) Math.round(distance * 10);
        }
        constraintScores.put("SOFT: ZSP Course Distribution", softScore);
        return softScore;
    }

    private int calculateBudgetConstraint(InternshipSolution solution, List<PlannedInternship> internships) {
        long activeCount = internships.stream().filter(PlannedInternship::isActive).count();
        int budget = solution.getBudget().getMaxActiveInternships();
        int deviation = (int) Math.abs(activeCount - budget);
        int score = -deviation * 1000;
        recordConstraint("HARD: Budget (active=" + activeCount + ", budget=" + budget + ", deviation=" + deviation + ")", score);
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
                hardScore -= deficit * 200; // Penalize each missing active slot
            }
        }

        recordConstraint("HARD: Minimum Activation Requirements", hardScore);
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
        int violations = 0;
        for (long count : teacherCounts.values()) {
            if (count == 3 || count > 4) {
                hardScore -= count; // Penalize invalid workload
                violations++;
            }
        }

        recordConstraint("HARD: Teacher Workload (" + violations + " teachers with invalid load)", hardScore);
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
            if (count == 2 || count == 4) {
                long uniqueTypes = assignments.stream()
                    .map(PlannedInternship::getPraktikumType)
                    .distinct()
                    .count();
                if (uniqueTypes != count) {
                    hardScore -= 20; // Penalize if not all different
                }
            }
        }

        recordConstraint("HARD: Teacher Diversity (types must differ)", hardScore);
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
                hardScore -= 1;
            }
        }

        recordConstraint("HARD: Teacher Praktikum Type Support", hardScore);
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
                    hardScore -= 1; // Teacher doesn't support this course
                }
            }
        }

        recordConstraint("HARD: Teacher Course Match (ZSP/SFP)", hardScore);
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

        recordConstraint("HARD: Course Pinning (PDP null, SFP fixed)", hardScore);
        return hardScore;
    }

    private int calculateZspCourseAssignmentConstraint(List<PlannedInternship> internships) {
        int hardScore = 0;
        int violations = 0;

        for (PlannedInternship i : internships) {
            // Active ZSP internships must have a course assigned
            if (i.isActive() && i.getPraktikumType() == PraktikumType.ZSP && i.getCourse() == null) {
                hardScore -= 50; // Penalty for ZSP without course
                violations++;
            }
        }

        recordConstraint("HARD: ZSP Course Assignment (" + violations + " ZSP without course)", hardScore);
        return hardScore;
    }

    private int calculateSchoolTypeMatchConstraint(List<PlannedInternship> internships) {
        int hardScore = 0;

        int violations = 0;
        for (PlannedInternship i : internships) {
            if (i.isActive() && i.getSchool() != null && i.getSchool().getType() != i.getSchoolType()) {
                hardScore -= 100; // School type doesn't match requirement
                violations++;
            }
        }

        recordConstraint("HARD: School Type Match (" + violations + " mismatches)", hardScore);
        return hardScore;
    }

    private int calculateTeacherAssignmentConstraint(List<PlannedInternship> internships) {
        int hardScore = 0;

        int violations = 0;
        for (PlannedInternship i : internships) {
            if (i.isActive() && i.getAssignedTeacher() == null) {
                hardScore -= 100; // Active internship without assigned teacher
                violations++;
            }
        }

        recordConstraint("HARD: Teacher Assignment (" + violations + " active without teacher)", hardScore);
        return hardScore;
    }

    private int calculateZoneConstraint(List<PlannedInternship> internships) {
        int hardScore = 0;

        for (PlannedInternship i : internships) {
            if (!i.isActive() || i.getSchool() == null) {
                continue;
            }

            PraktikumType type = i.getPraktikumType();
            String zone = i.getSchool().getZone();
            Boolean hasOepnv = i.getSchool().getOepnv();

            boolean isViolation = false;
            if (type == PraktikumType.ZSP || type == PraktikumType.SFP) {
                // Zone 1 is OK
                if ("1".equals(zone)) {
                    isViolation = false;
                }
                // Zone 2 with OEPNV is OK
                else if ("2".equals(zone) && Boolean.TRUE.equals(hasOepnv)) {
                    isViolation = false;
                }
                // Everything else is NOT OK
                else {
                    isViolation = true;
                }
            } else { // PDP_I or PDP_II
                // Zone 2 or 3 is OK
                if ("2".equals(zone) || "3".equals(zone)) {
                    isViolation = false;
                }
                // Zone 1 is NOT OK
                else {
                    isViolation = true;
                }
            }

            if (isViolation) {
                hardScore -= 1;
            }
        }

        recordConstraint("HARD: Zone Rules (ZSP/SFP zone 1/2+OEPNV, PDP zone 2/3)", hardScore);
        return hardScore;
    }

    private int calculateTeacher2InternshipsPreference(List<PlannedInternship> internships) {
        int softScore = 0;

        Map<Teacher, Long> teacherCounts = internships.stream()
            .filter(i -> i.isActive() && i.getAssignedTeacher() != null)
            .collect(Collectors.groupingBy(PlannedInternship::getAssignedTeacher, Collectors.counting()));

        // Reward teachers with exactly 2 internships
        int teachersWith2 = 0;
        for (long count : teacherCounts.values()) {
            if (count == 2) {
                softScore += 10; // Bonus points for ideal workload
                teachersWith2++;
            }
        }

        recordConstraint("SOFT: Teacher 2 Internships Preference (" + teachersWith2 + " teachers)", softScore);
        return softScore;
    }

    private int calculateDiversityPreference(List<PlannedInternship> internships) {
        int softScore = 0;

        // Group by school and type, count occurrences
        Map<String, Long> typeBySchool = new HashMap<>();
        
        for (PlannedInternship i : internships) {
            if (i.isActive() && i.getSchool() != null) {
                String key = i.getSchool().getId() + "/" + i.getPraktikumType();
                typeBySchool.merge(key, 1L, Long::sum);
            }
        }

        // Penalize if same type appears multiple times in same school
        int repetitions = 0;
        for (long count : typeBySchool.values()) {
            if (count > 1) {
                softScore -= (count - 1) * 2; // Penalize repeated types
                repetitions += (count - 1);
            }
        }

        recordConstraint("SOFT: Type Diversity Per School (" + repetitions + " repetitions)", softScore);
        return softScore;
    }

    private int calculateZspCourseDistributionPreference(InternshipSolution solution, List<PlannedInternship> internships) {
        int softScore = 0;

        ZspCourseDistribution distribution = solution.getZspCourseDistribution();
        if (distribution == null) {
            return 0;
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

        recordConstraint("SOFT: ZSP Course Distribution", softScore);
        return softScore;
    }
}
