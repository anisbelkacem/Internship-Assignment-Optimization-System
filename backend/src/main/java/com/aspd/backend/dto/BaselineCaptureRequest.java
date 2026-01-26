package com.aspd.backend.dto;

import lombok.*;

/**
 * Request DTO for capturing a baseline snapshot of current assignments.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BaselineCaptureRequest {

    /**
     * School year with semester notation (e.g., "WiSe2025", "SoSe2025").
     */
    private String schoolYear;

    /**
     * Whether to overwrite existing baseline if one already exists.
     */
    private boolean overwriteExisting;

    /**
     * Optional notes about why this baseline is being captured.
     */
    private String notes;

    /**
     * Which student demand IDs to include. If null/empty, captures all.
     */
    private java.util.List<Long> studentDemandIds;
}
