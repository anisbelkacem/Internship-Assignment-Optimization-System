package com.aspd.backend.solver;

import com.aspd.backend.model.*;
import org.optaplanner.core.api.domain.solution.PlanningEntityCollectionProperty;
import org.optaplanner.core.api.domain.solution.PlanningScore;
import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.domain.solution.ProblemFactCollectionProperty;
import org.optaplanner.core.api.domain.valuerange.ValueRangeProvider;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import lombok.*;

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
     * All schools that can host internships.
     * Each school has a type (GS/MS) and geographic zone.
     */
    @ValueRangeProvider(id = "schoolRange")
    @ProblemFactCollectionProperty
    private List<School> availableSchools;

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
    
    private Integer timeBudget; // Total hours available (e.g., 210)
}