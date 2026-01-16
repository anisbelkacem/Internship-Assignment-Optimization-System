package com.aspd.backend.solver;

import com.aspd.backend.model.*;
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
}