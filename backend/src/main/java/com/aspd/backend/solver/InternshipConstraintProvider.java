package com.aspd.backend.solver;
import com.aspd.backend.model.Course;
import com.aspd.backend.model.InternshipBudget;
import com.aspd.backend.model.InternshipTypeRequirement;
import com.aspd.backend.model.PlannedInternship;
import com.aspd.backend.model.PraktikumType;
import com.aspd.backend.model.SchoolType;
import com.aspd.backend.model.Teacher;
import com.aspd.backend.model.TeacherPlConfig;
import com.aspd.backend.model.ZspCourseDistribution;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import org.optaplanner.core.api.score.stream.*;

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

    @Override
    public Constraint[] defineConstraints(ConstraintFactory constraintFactory) {
        return new Constraint[]{
            // HARD CONSTRAINTS - Internship Activation & Budget
            respectTotalInternshipBudget(constraintFactory),

                // HARD CONSTRAINTS - Minimum activation by type
                ensureMinimumPdpIActivatedPerSchoolType(constraintFactory),
                ensureMinimumPdpIIActivatedPerSchoolType(constraintFactory),
                ensureMinimumZspActivatedPerSchoolType(constraintFactory),
                ensureMinimumSfpActivatedPerSchoolCourse(constraintFactory),

                // HARD CONSTRAINTS - Teacher Assignment Rules (only for active internships)
                teacherCanOnlyTake_0_1_2_or_4_Internships(constraintFactory),
                teacherWith_2_or_4_InternshipsMustHaveAllDifferentTypes(constraintFactory),
                teacherMustSupportPraktikumType(constraintFactory),
                teacherMustMatchCourseForZspAndSfp(constraintFactory),
        
            // school type must match
                schoolTypeMustMatch(constraintFactory),
                                
                // teacher must be assigned (only if active)
                teacherMustBeAssigned(constraintFactory),
                // HARD CONSTRAINTS - School and Zone Rules
                internshipsMustBeInAcceptableZones(constraintFactory),

                // SOFT CONSTRAINTS - Basic Assignment
                //rewardTeacherAssignment(constraintFactory),

                // SOFT CONSTRAINTS - Workload Optimization
                preferTeachersWithExactly2Internships(constraintFactory),

                // SOFT CONSTRAINTS - Diversity
                preferDiverseInternshipTypesPerSchool(constraintFactory),
                
                // SOFT CONSTRAINTS - ZSP Course Preferences
                alignZspCourseDistributionWithPreferences(constraintFactory),
        };
    }

    // ================================================================================
    // HARD CONSTRAINTS - Internship Activation & Budget
    // ================================================================================

    // ================================================================================
    // HARD CONSTRAINTS - Internship Activation & Budget
    // ================================================================================

    /**
     * HARD: Total active internships must equal the budget.
     *
     * The solver must activate exactly budget.maxActiveInternships slots.
     * More or fewer activations are penalized proportionally.
     * 
     * This aggregates the active count FIRST, then joins with budget to ensure
     * the penalty is applied exactly once, not multiplied by each match.
     */
    Constraint respectTotalInternshipBudget(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(PlannedInternship.class)
                .groupBy(ConstraintCollectors.sum(pi -> pi.isActive() ? 1 : 0))
                .join(InternshipBudget.class)
                .filter((activeCount, budget) -> activeCount != budget.getMaxActiveInternships())
                .penalize(HardSoftScore.ONE_HARD, (activeCount, budget) -> {
                    int deviation = Math.abs(activeCount - budget.getMaxActiveInternships());
                    return deviation * 1000;
                })
                .asConstraint("respectTotalInternshipBudget");
    }



    /**
     * HARD: At least half of PDP_I slots per school type must be active.
     * Uses pre-calculated requirements to avoid groupBy caching issues.
     */
    Constraint ensureMinimumPdpIActivatedPerSchoolType(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(InternshipTypeRequirement.class)
                .filter(req -> req.getType() == PraktikumType.PDP_I)
                .join(constraintFactory.forEach(PlannedInternship.class)
                        .filter(i -> i.getPraktikumType() == PraktikumType.PDP_I),
                        Joiners.equal(req -> req.getSchoolType(), PlannedInternship::getSchoolType))
                .groupBy((req, internships) -> req,
                        ConstraintCollectors.countDistinct((req, i) -> i.isActive() ? i : null))
                .filter((req, activeCount) -> activeCount < req.getRequiredActive())
                .penalize(HardSoftScore.ONE_HARD, (req, activeCount) -> 
                        Math.max(0, req.getRequiredActive() - activeCount.intValue()) * 200)
                .asConstraint("ensureMinimumPdpIActivatedPerSchoolType");
    }

    /**
     * HARD: At least half of PDP_II slots per school type must be active.
     * Uses pre-calculated requirements to avoid groupBy caching issues.
     */
    Constraint ensureMinimumPdpIIActivatedPerSchoolType(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(InternshipTypeRequirement.class)
                .filter(req -> req.getType() == PraktikumType.PDP_II)
                .join(constraintFactory.forEach(PlannedInternship.class)
                        .filter(i -> i.getPraktikumType() == PraktikumType.PDP_II),
                        Joiners.equal(req -> req.getSchoolType(), PlannedInternship::getSchoolType))
                .groupBy((req, internships) -> req,
                        ConstraintCollectors.countDistinct((req, i) -> i.isActive() ? i : null))
                .filter((req, activeCount) -> activeCount < req.getRequiredActive())
                .penalize(HardSoftScore.ONE_HARD, (req, activeCount) -> 
                        Math.max(0, req.getRequiredActive() - activeCount.intValue()) * 200)
                .asConstraint("ensureMinimumPdpIIActivatedPerSchoolType");
    }

    /**
     * HARD: At least a quarter of ZSP slots per school type must be active.
     * Uses pre-calculated requirements to avoid groupBy caching issues.
     */
    Constraint ensureMinimumZspActivatedPerSchoolType(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(InternshipTypeRequirement.class)
                .filter(req -> req.getType() == PraktikumType.ZSP)
                .join(constraintFactory.forEach(PlannedInternship.class)
                        .filter(i -> i.getPraktikumType() == PraktikumType.ZSP),
                        Joiners.equal(req -> req.getSchoolType(), PlannedInternship::getSchoolType))
                .groupBy((req, internships) -> req,
                        ConstraintCollectors.countDistinct((req, i) -> i.isActive() ? i : null))
                .filter((req, activeCount) -> activeCount < req.getRequiredActive())
                .penalize(HardSoftScore.ONE_HARD, (req, activeCount) -> 
                        Math.max(0, req.getRequiredActive() - activeCount.intValue()) * 200)
                .asConstraint("ensureMinimumZspActivatedPerSchoolType");
    }

    /**
     * HARD: At least a quarter of SFP slots per (school type, course) must be active.
     * Uses pre-calculated requirements to avoid groupBy caching issues.
     */
    Constraint ensureMinimumSfpActivatedPerSchoolCourse(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(InternshipTypeRequirement.class)
                .filter(req -> req.getType() == PraktikumType.SFP && req.getCourse() != null)
                .join(constraintFactory.forEach(PlannedInternship.class)
                        .filter(i -> i.getPraktikumType() == PraktikumType.SFP && i.getCourse() != null),
                        Joiners.equal(req -> req.getSchoolType(), PlannedInternship::getSchoolType),
                        Joiners.equal(req -> req.getCourse(), PlannedInternship::getCourse))
                .groupBy((req, internships) -> req,
                        ConstraintCollectors.countDistinct((req, i) -> i.isActive() ? i : null))
                .filter((req, activeCount) -> activeCount < req.getRequiredActive())
                .penalize(HardSoftScore.ONE_HARD, (req, activeCount) -> 
                        Math.max(0, req.getRequiredActive() - activeCount.intValue()) * 200)
                .asConstraint("ensureMinimumSfpActivatedPerSchoolCourse");
    }

    // ================================================================================
    // HARD CONSTRAINTS - Teacher Assignment Rules
    // ================================================================================

    /**
     * HARD: A teacher may take 0, 1, 2, or 4 internships (not 3, not 5+).
     *
     * This constraint enforces valid workload distribution. Teachers cannot
     * take exactly 3 internships or more than 4 internships.
     * Only counts ACTIVE internships.
     */
    Constraint teacherCanOnlyTake_0_1_2_or_4_Internships(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(PlannedInternship.class)
                .filter(internship -> internship.isActive() && internship.getAssignedTeacher() != null)
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
                .filter(internship -> internship.isActive() && internship.getAssignedTeacher() != null)
                .groupBy(PlannedInternship::getAssignedTeacher, 
                         ConstraintCollectors.toList())
                .filter((teacher, internships) -> {
                    int count = internships.size();
                    if (count != 2 && count != 4) 
                        {
                            return false;
                        }
                    
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
                .filter(internship -> internship.isActive() && internship.getAssignedTeacher() != null)
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
                        return true; // No config = violates constraint
                    }
                    
                    // Check if teacher's preferences include this type
                    return !config.getInternshipPreferences().contains(type);
                })
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("teacherMustSupportPraktikumType");
    }

    /**
     * HARD: For SFP, teacher must match the course requirement.
     *
     * Teachers may only supervise SFP internships in subjects from:
     * - Their main subject (Teacher.mainSubject), OR
     * - Their subject specializations (TeacherPlConfig.subjectSpecializations)
     *
     * Note: ZSP course matching is now handled via soft constraints using weighted preference distribution.
     * This does not apply to PDP_I and PDP_II internships.
     */
    Constraint teacherMustMatchCourseForZspAndSfp(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(PlannedInternship.class)
                .filter(internship -> internship.isActive() && internship.getAssignedTeacher() != null)
                .filter(internship -> internship.getCourse() != null) // Only SFP has course set
                .filter(internship -> internship.getPraktikumType() == PraktikumType.SFP)
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

                    return config == null || !config.getSubjectSpecializations().contains(requiredCourse);
                })
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("teacherMustMatchCourseForSfp");
    }

    // Teacher belongs to exactly one school; school is derived from teacher, so no constraint needed.

    /**
     * HARD: Active internships must have a teacher assigned.
     * Inactive internships are not subject to this constraint.
    */
    Constraint teacherMustBeAssigned(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(PlannedInternship.class)
                .filter(internship -> internship.isActive() && internship.getAssignedTeacher() == null)
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("teacher must be assigned");
    }

    
    // ================================================================================
    // HARD CONSTRAINTS - School and Zone Rules
    // ================================================================================
    /**
     * HARD: School type must match.
     * Only applies to active internships.
    */
    Constraint schoolTypeMustMatch(ConstraintFactory constraintFactory) {
        return constraintFactory
            .forEach(PlannedInternship.class)
            .filter(internship -> internship.isActive() && internship.getSchool() != null && internship.getSchool().getType() != internship.getSchoolType())
            .penalize(HardSoftScore.ONE_HARD)
            .asConstraint("School type must match");
    }
    /**
     * HARD: Internships must be located in acceptable geographic zones.
     * Only applies to active internships.
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
            .filter(internship -> internship.isActive() && internship.getSchool() != null)
                .filter(internship -> {
                    PraktikumType type = internship.getPraktikumType();
                String zone = internship.getSchool().getZone();
                Boolean hasOepnv = internship.getSchool().getOepnv();
                    
                    if (type == PraktikumType.ZSP || type == PraktikumType.SFP) {
                        // Zone 1 is OK
                        if ("1".equals(zone)) {
                            return false;
                        }
                        // Zone 2 with OEPNV is OK
                        if ("2".equals(zone) && Boolean.TRUE.equals(hasOepnv)){
                            return false;
                        } 
                        
                        // Everything else is NOT OK
                        return true;
                    } else { // PDP_I or PDP_II
                        // Zone 2 or 3 is OK
                        if ("2".equals(zone) || "3".equals(zone)){
                            return false;
                        }
                        
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
                .filter(internship -> internship.isActive() && internship.getAssignedTeacher() != null)
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
                .filter(internship -> internship.isActive() && internship.getAssignedTeacher() != null)
                .groupBy(PlannedInternship::getAssignedTeacher, ConstraintCollectors.count())
                .filter((teacher, count) -> count == IDEAL_TEACHER_INTERNSHIPS)
                .reward(HardSoftScore.ONE_SOFT, (teacher, count) -> 10) // Bonus points
                .asConstraint("preferTeachersWithExactly2Internships");
    }

    /**
     * SOFT: Prefer diverse internship types within the same school.
     * Only counts active internships.
     *
     * If multiple teachers are assigned to the same school:
     * - Homogeneous types are penalized (e.g., 5 × ZSP in same school = low score)
     * - Mixed types are preferred (e.g., 3 × ZSP + 2 × SFP = higher score)
     */
    Constraint preferDiverseInternshipTypesPerSchool(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(PlannedInternship.class)
            .filter(internship -> internship.isActive() && internship.getSchool() != null)
            .groupBy(PlannedInternship::getSchool,
                         PlannedInternship::getPraktikumType,
                         ConstraintCollectors.count())
                .filter((school, type, count) -> count > 1)
                .penalize(HardSoftScore.ONE_SOFT, (school, type, count) -> (count.intValue() - 1) * 2)
                .asConstraint("preferDiverseInternshipTypesPerSchool");
    }

    // ================================================================================
    // SOFT CONSTRAINTS - ZSP Course Preferences
    // ================================================================================

    /**
     * SOFT: Align ZSP course distribution with student preferences.
     *
     * For each active ZSP internship with an assigned teacher, we tally the courses
     * by school type (GS/MS) and compare against the weighted preference distribution.
     * 
     * The constraint measures the distance between:
     * - Actual: Count of active ZSP internships per course
     * - Desired: Weighted preference sum from student configs (main=0.5, pref1=0.3, etc.)
     * 
     * Note: Multiple students may be assigned to the same internship slot, so actual
     * counts may be lower than preference weights. This is an approximation.
     */
    Constraint alignZspCourseDistributionWithPreferences(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(PlannedInternship.class)
                .filter(internship -> internship.isActive() 
                        && internship.getPraktikumType() == PraktikumType.ZSP
                        && internship.getAssignedTeacher() != null)
                .groupBy(PlannedInternship::getSchoolType,
                         internship -> internship.getAssignedTeacher().getMainSubject(),
                         ConstraintCollectors.count())
                .join(ZspCourseDistribution.class)
                .penalize(HardSoftScore.ONE_SOFT, (schoolType, course, actualCount, distribution) -> {
                    if (course == null) {
                        return 0; // No penalty if teacher has no main subject
                    }
                    
                    // Get the desired weight from the appropriate distribution map
                    double desiredWeight;
                    if (schoolType == SchoolType.GS) {
                        desiredWeight = distribution.getGsDistribution().getOrDefault(course, 0.0);
                    } else {
                        desiredWeight = distribution.getMsDistribution().getOrDefault(course, 0.0);
                    }
                    
                    // Calculate distance: |actual - desired|
                    // Scale by 10 to make the penalty more significant
                    double distance = Math.abs(actualCount.intValue() - desiredWeight);
                    return (int) Math.round(distance * 10);
                })
                .asConstraint("alignZspCourseDistributionWithPreferences");
    }
}