package com.aspd.backend.solver;

import com.aspd.backend.model.*;
import com.aspd.backend.dto.CoordinatesDto;
import org.optaplanner.core.api.domain.solution.PlanningEntityCollectionProperty;
import org.optaplanner.core.api.domain.solution.PlanningScore;
import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.domain.solution.ProblemFactCollectionProperty;
import org.optaplanner.core.api.domain.solution.ProblemFactProperty;
import org.optaplanner.core.api.domain.valuerange.ValueRangeProvider;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import lombok.*;

import java.util.Arrays;
import java.util.List;

@PlanningSolution
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
// This is the entire problem for FIRST OPTIMIZATION: assigning teachers to planned internships
public class InternshipSolution {

    // AVAILABLE RESOURCES (Problem Facts)
    
    /**
     * All teachers available to supervise internships.
     * Each teacher has preferences and constraints (TeacherPlConfig).
     */
    @ValueRangeProvider(id = "teacherRange")
    @ProblemFactCollectionProperty
    private List<Teacher> availableTeachers;

    /**
     * All courses available for ZSP assignment.
     * OptaPlanner will select appropriate courses for ZSP internships.
     */
    @ValueRangeProvider(id = "courseRange")
    @ProblemFactCollectionProperty
    private List<Course> availableCourses;

    /**
     * Precomputed student coordinates for PDP distance soft constraint.
     * GS and MS are kept separate to match school type.
     */
    @ProblemFactCollectionProperty
    private List<CoordinatesDto> pdpGsStudentCoords;

    @ProblemFactCollectionProperty
    private List<CoordinatesDto> pdpMsStudentCoords;

    // School is derived from the assigned teacher; no explicit school range needed.
    /**
     * Boolean range for active/inactive decision.
     */
    @ValueRangeProvider(id = "booleanRange")
    public List<Boolean> getBooleanRange() {
        return Arrays.asList(true, false);
    }

    // PLANNING ENTITIES TO OPTIMIZE
    
    /**
     * The planned internships that need teachers and schools assigned.
     * These are created based on student demand (StudentConfig data).
     * OptaPlanner will assign Teacher + School to each.
     */
    @PlanningEntityCollectionProperty
    private List<PlannedInternship> plannedInternships;

    // SCORE (OptaPlanner calculates this based on constraints)
    @PlanningScore
    private HardSoftScore score;

    // METADATA
    private String schoolYear; // e.g., "2025"
    
    @ProblemFactProperty
    private Integer timeBudget; // Total active internship slots budget (e.g., 25)
    
    @ProblemFactProperty
    private InternshipBudget budget; // Budget wrapper for constraint access
    
    @ProblemFactProperty
    private ZspCourseDistribution zspCourseDistribution; // Weighted ZSP course preferences
    
    /**
     * Previous semester's teacher assignments for preservation during reoptimization.
     * Key: praktikumType/schoolType/course -> Teacher
     * Used to reward keeping the same teacher assignments when possible.
     */
    @ProblemFactCollectionProperty
    private List<PlannedInternship> previousSemesterInternships;
}