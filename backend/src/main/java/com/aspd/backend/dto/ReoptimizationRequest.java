package com.aspd.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for re-optimization endpoint.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReoptimizationRequest {
    
    /**
     * School year with semester notation (e.g., "SoSe2025" for summer 2025).
     * The system will automatically load the previous semester's baseline
     * (e.g., "WiSe2025" for winter 2025).
     */
    private String schoolYear;
    
    /**
     * Total internship budget (maximum number of internship slots).
     * For summer semester, available budget = internshipBudget - winterBudgetUsed + uncompletedInternships.
     */
    private Integer internshipBudget;
    
    /**
     * Number of uncompleted internships from the previous semester (winter).
     * These internships were not completed and need to be added to the summer semester budget.
     */
    private Integer uncompletedInternships;
    
    /**
     * Solver time limit in seconds for Phase 1 optimization (60-43200).
     * Used for async reoptimization. Defaults to 300 (5 minutes).
     */
    private Long phase1TimeLimitSeconds;
}
