package com.aspd.backend.solver;

import com.aspd.backend.model.Course;
import com.aspd.backend.model.PlannedInternship;
import com.aspd.backend.model.PraktikumType;
import com.aspd.backend.model.Teacher;
import com.aspd.backend.model.TeacherPlConfig;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import org.optaplanner.core.api.score.stream.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.aspd.backend.common.constants.InternshipConstants.*;

/**
 * Constraint provider for internship planning and assignment optimization.
 *
 * This class defines both hard constraints (must be satisfied) and soft constraints
 * (preferences to optimize) for the internship assignment problem.
 * 
 * PHASE 1: Teacher-to-Internship Assignment
 * - Input: PlannedInternships (slots that need teachers)
 * - Output: Each PlannedInternship has an assigned Teacher and School
 */
public class InternshipConstraintProvider implements ConstraintProvider {
    
    private static final Logger log = LoggerFactory.getLogger(InternshipConstraintProvider.class);

    @Override
    public Constraint[] defineConstraints(ConstraintFactory constraintFactory) {
        return new Constraint[]{
                // HARD CONSTRAINTS - Teacher Assignment Rules
                teacherCanOnlyTake_0_1_2_or_4_Internships(constraintFactory),
                teacherWith_2_or_4_InternshipsMustHaveAllDifferentTypes(constraintFactory),
                teacherMustSupportPraktikumType(constraintFactory),
                teacherMustMatchCourseForZspAndSfp(constraintFactory),

                // HARD CONSTRAINTS - School and Zone Rules
                internshipsMustBeInAcceptableZones(constraintFactory),

                // SOFT CONSTRAINTS - Basic Assignment
                rewardTeacherAssignment(constraintFactory),

                // SOFT CONSTRAINTS - Workload Optimization
                preferTeachersWithExactly2Internships(constraintFactory),

                // SOFT CONSTRAINTS - Diversity
                preferDiverseInternshipTypesPerSchool(constraintFactory),
        };
    }

    // ================================================================================
    // HARD CONSTRAINTS - Teacher Assignment Rules
    // ================================================================================

    /**
     * HARD: A teacher may take 0, 1, 2, or 4 internships (not 3, not 5+).
     *
     * This constraint enforces valid workload distribution. Teachers cannot
     * take exactly 3 internships or more than 4 internships.
     */
    Constraint teacherCanOnlyTake_0_1_2_or_4_Internships(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(PlannedInternship.class)
                .filter(internship -> internship.getAssignedTeacher() != null)
                .groupBy(PlannedInternship::getAssignedTeacher, ConstraintCollectors.count())
                .filter((teacher, count) -> count == 3 || count > 4)
                .penalize(HardSoftScore.ONE_HARD, (teacher, count) -> count.intValue())
                .asConstraint("teacherCanOnlyTake_0_1_2_or_4_Internships");
    }

    /**
     * HARD: If a teacher takes 2 or 4 internships, they must be all of different types.
     *
     * This ensures workload diversity. A teacher with 2 internships cannot supervise
     * 2 × ZSP; they must supervise different types (e.g., ZSP + SFP).
     */
    Constraint teacherWith_2_or_4_InternshipsMustHaveAllDifferentTypes(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(PlannedInternship.class)
                .filter(internship -> internship.getAssignedTeacher() != null)
                .groupBy(PlannedInternship::getAssignedTeacher, 
                         ConstraintCollectors.toList())
                .filter((teacher, internships) -> {
                    int count = internships.size();
                    if (count != 2 && count != 4) return false;
                    
                    // Check if all types are different
                    long uniqueTypes = internships.stream()
                            .map(PlannedInternship::getPraktikumType)
                            .distinct()
                            .count();
                    
                    return uniqueTypes != count; // Penalize if not all different
                })
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("teacherWith_2_or_4_InternshipsMustHaveAllDifferentTypes");
    }

    /**
     * HARD: Teacher may only take internship types listed in their preferences.
     *
     * Each teacher has a TeacherPlConfig for the school year with a set of
     * internshipPreferences (e.g., {ZSP, SFP}). A teacher can only be assigned
     * internships of types they have indicated they can supervise.
     */
    Constraint teacherMustSupportPraktikumType(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(PlannedInternship.class)
                .filter(internship -> internship.getAssignedTeacher() != null)
                .filter(internship -> {
                    Teacher teacher = internship.getAssignedTeacher();
                    String schoolYear = internship.getSchoolYear();
                    PraktikumType type = internship.getPraktikumType();
                    
                    // Find the teacher's config for this school year
                    TeacherPlConfig config = teacher.getPlConfigs().stream()
                            .filter(c -> c.getSchoolYear().equals(schoolYear))
                            .findFirst()
                            .orElse(null);
                    
                    if (config == null) {
                        log.warn("CONSTRAINT VIOLATION: Teacher {} has no config for year {}", 
                                teacher.getTeacherId(), schoolYear);
                        return true; // No config = violates constraint
                    }
                    
                    // Check if teacher's preferences include this type
                    boolean violation = !config.getInternshipPreferences().contains(type);
                    if (violation) {
                        log.warn("CONSTRAINT VIOLATION: Teacher {} does not support {} (preferences: {})",
                                teacher.getTeacherId(), type, config.getInternshipPreferences());
                    }
                    return violation;
                })
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("teacherMustSupportPraktikumType");
    }

    /**
     * HARD: For ZSP and SFP, teacher must match the course requirement.
     *
     * Teachers may only supervise ZSP/SFP internships in subjects from:
     * - Their main subject (Teacher.mainSubject), OR
     * - Their subject specializations (TeacherPlConfig.subjectSpecializations)
     *
     * This does not apply to PDP_I and PDP_II internships.
     */
    Constraint teacherMustMatchCourseForZspAndSfp(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(PlannedInternship.class)
                .filter(internship -> internship.getAssignedTeacher() != null)
                .filter(internship -> internship.getCourse() != null) // Only ZSP/SFP have courses (PDP has null)
                .filter(internship -> {
                    PraktikumType type = internship.getPraktikumType();
                    return type == PraktikumType.ZSP || type == PraktikumType.SFP;
                })
                .filter(internship -> {
                    Teacher teacher = internship.getAssignedTeacher();
                    Course requiredCourse = internship.getCourse();
                    String schoolYear = internship.getSchoolYear();
                    
                    // Check if main subject matches
                    if (teacher.getMainSubject() == requiredCourse) {
                        return false; // No violation
                    }
                    
                    // Check if any specialization matches
                    TeacherPlConfig config = teacher.getPlConfigs().stream()
                            .filter(c -> c.getSchoolYear().equals(schoolYear))
                            .findFirst()
                            .orElse(null);
                    
                    if (config != null && config.getSubjectSpecializations().contains(requiredCourse)) {
                        return false; // No violation
                    }
                    
                    log.warn("CONSTRAINT VIOLATION: Teacher {} (main: {}, specs: {}) doesn't match course {} for {}",
                            teacher.getTeacherId(), teacher.getMainSubject(), 
                            config != null ? config.getSubjectSpecializations() : "no config",
                            requiredCourse, internship.getPraktikumType());
                    return true; // Violation: teacher doesn't match course
                })
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("teacherMustMatchCourseForZspAndSfp");
    }

    // ================================================================================
    // HARD CONSTRAINTS - School and Zone Rules
    // ================================================================================

    /**
     * HARD: Internships must be located in acceptable geographic zones.
     *
     * Zone requirements by praktikum type:
     * - ZSP / SFP:
     *   - Preferred: Zone 1
     *   - Acceptable: Zone 2, provided public transport access (OEPNV = true)
     *   - Not allowed: Zone 3 or Zone 2 without public transport
     *
     * - PDP_I / PDP_II:
     *   - Acceptable: Zone 2 or Zone 3
     *   - Not allowed: Zone 1
     */
    Constraint internshipsMustBeInAcceptableZones(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(PlannedInternship.class)
                .filter(internship -> internship.getAssignedSchool() != null)
                .filter(internship -> {
                    PraktikumType type = internship.getPraktikumType();
                    String zone = internship.getAssignedSchool().getZone();
                    Boolean hasOepnv = internship.getAssignedSchool().getOepnv();
                    
                    if (type == PraktikumType.ZSP || type == PraktikumType.SFP) {
                        // Zone 1 is OK
                        if ("1".equals(zone)) return false;
                        
                        // Zone 2 with OEPNV is OK
                        if ("2".equals(zone) && Boolean.TRUE.equals(hasOepnv)) return false;
                        
                        // Everything else is NOT OK
                        return true;
                    } else { // PDP_I or PDP_II
                        // Zone 2 or 3 is OK
                        if ("2".equals(zone) || "3".equals(zone)) return false;
                        
                        // Zone 1 is NOT OK
                        return true;
                    }
                })
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("internshipsMustBeInAcceptableZones");
    }

    // ================================================================================
    // SOFT CONSTRAINTS - Basic Assignment
    // ================================================================================

    /**
     * SOFT: Reward each teacher assignment.
     * 
     * This ensures OptaPlanner prefers solutions with assignments over
     * the empty solution where all teachers are null.
     */
    Constraint rewardTeacherAssignment(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(PlannedInternship.class)
                .filter(internship -> internship.getAssignedTeacher() != null)
                .reward(HardSoftScore.ONE_SOFT, internship -> 100)
                .asConstraint("rewardTeacherAssignment");
    }

    // ================================================================================
    // SOFT CONSTRAINTS - Optimization Goals
    // ================================================================================

    /**
     * SOFT: Prefer teachers supervising exactly 2 internships.
     *
     * The ideal workload is 2 internships per teacher. Teachers with exactly
     * 2 internships receive a score bonus.
     */
    Constraint preferTeachersWithExactly2Internships(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(PlannedInternship.class)
                .filter(internship -> internship.getAssignedTeacher() != null)
                .groupBy(PlannedInternship::getAssignedTeacher, ConstraintCollectors.count())
                .filter((teacher, count) -> count == IDEAL_TEACHER_INTERNSHIPS)
                .reward(HardSoftScore.ONE_SOFT, (teacher, count) -> 10) // Bonus points
                .asConstraint("preferTeachersWithExactly2Internships");
    }

    /**
     * SOFT: Prefer diverse internship types within the same school.
     *
     * If multiple teachers are assigned to the same school:
     * - Homogeneous types are penalized (e.g., 5 × ZSP in same school = low score)
     * - Mixed types are preferred (e.g., 3 × ZSP + 2 × SFP = higher score)
     */
    Constraint preferDiverseInternshipTypesPerSchool(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(PlannedInternship.class)
                .filter(internship -> internship.getAssignedSchool() != null)
                .groupBy(PlannedInternship::getAssignedSchool,
                         PlannedInternship::getPraktikumType,
                         ConstraintCollectors.count())
                .filter((school, type, count) -> count > 1)
                .penalize(HardSoftScore.ONE_SOFT, (school, type, count) -> (count.intValue() - 1) * 2)
                .asConstraint("preferDiverseInternshipTypesPerSchool");
    }
}