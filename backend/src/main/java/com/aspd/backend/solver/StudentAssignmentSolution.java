package com.aspd.backend.solver;

import com.aspd.backend.model.PlannedInternship;
import com.aspd.backend.model.StudentInternshipDemand;
import org.optaplanner.core.api.domain.solution.PlanningEntityCollectionProperty;
import org.optaplanner.core.api.domain.solution.PlanningScore;
import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.domain.solution.ProblemFactCollectionProperty;
import org.optaplanner.core.api.domain.valuerange.ValueRangeProvider;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import lombok.*;

import java.util.List;

/**
 * Planning solution for PHASE 2: Assigning students to planned internships.
 * 
 * This phase runs AFTER Phase 1 (teacher-to-internship assignment).
 * 
 * Input:
 * - PlannedInternships (already have assigned teachers and schools from Phase 1)
 * - StudentInternshipDemands (students who need internships)
 * 
 * Output:
 * - Each StudentInternshipDemand has an assigned PlannedInternship
 * - PlannedInternships track how many students are assigned (currentAssignments)
 */
@PlanningSolution
@NoArgsConstructor
@AllArgsConstructor
public class StudentAssignmentSolution {

    // AVAILABLE RESOURCES (Problem Facts from Phase 1)
    
    /**
     * All planned internships with teachers and schools already assigned.
     * These are the OUTPUT from Phase 1, now used as INPUT for Phase 2.
     */
    @ValueRangeProvider(id = "internshipRange")
    @ProblemFactCollectionProperty
    private List<PlannedInternship> availableInternships;

    // PLANNING ENTITIES TO OPTIMIZE
    
    /**
     * Student demands that need to be assigned to internships.
     * OptaPlanner will assign each demand to a suitable PlannedInternship.
     */
    @PlanningEntityCollectionProperty
    private List<StudentInternshipDemand> studentDemands;

    // SCORE (OptaPlanner calculates this based on Phase 2 constraints)
    @PlanningScore
    private HardSoftScore score;

    // METADATA
    private String schoolYear; // e.g., "2024/2025"
    
    private Integer timeBudget; // Total hours available for validation
    
    // EXPLICIT GETTERS AND SETTERS
    
    public List<PlannedInternship> getAvailableInternships() {
        return availableInternships;
    }
    
    public void setAvailableInternships(List<PlannedInternship> availableInternships) {
        this.availableInternships = availableInternships;
    }
    
    public List<StudentInternshipDemand> getStudentDemands() {
        return studentDemands;
    }
    
    public void setStudentDemands(List<StudentInternshipDemand> studentDemands) {
        this.studentDemands = studentDemands;
    }
    
    public HardSoftScore getScore() {
        return score;
    }
    
    public void setScore(HardSoftScore score) {
        this.score = score;
    }
    
    public String getSchoolYear() {
        return schoolYear;
    }
    
    public void setSchoolYear(String schoolYear) {
        this.schoolYear = schoolYear;
    }
    
    public Integer getTimeBudget() {
        return timeBudget;
    }
    
    public void setTimeBudget(Integer timeBudget) {
        this.timeBudget = timeBudget;
    }
}
