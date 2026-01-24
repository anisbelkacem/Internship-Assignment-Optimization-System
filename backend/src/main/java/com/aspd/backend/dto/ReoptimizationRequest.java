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
     * Optional time budget for optimization in seconds.
     */
    private Integer timeBudget;
}
