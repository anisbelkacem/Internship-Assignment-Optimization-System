package com.aspd.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response DTO for re-optimization results.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReoptimizationResponse {
    
    /**
     * School year with semester notation (e.g., "SoSe2025")
     */
    private String schoolYear;
    
    /**
     * OptaPlanner score (hard/soft)
     */
    private String score;
    
    /**
     * Number of students assigned
     */
    private int studentsAssigned;
    
    /**
     * Number of assignments that were pinned (unchangeable)
     */
    private int assignmentsPinned;
    
    /**
     * Total student demands
     */
    private int totalDemands;
    
    /**
     * Assignment DTOs
     */
    private List<AssignmentDto> assignments;
    
    /**
     * Message describing the re-optimization result
     */
    private String message;
    
    /**
     * Budget used in winter semester (PDP_I + ZSP)
     * Calculated as 0.5 per unique internship type per teacher
     */
    private Double winterBudgetUsed;
    
    /**
     * Initial budget provided for optimization
     */
    private Integer initialBudget;
    
    /**
     * Final budget calculated after formula: initialBudget - winterBudgetUsed + uncompletedInternships
     */
    private Integer finalBudget;
}
