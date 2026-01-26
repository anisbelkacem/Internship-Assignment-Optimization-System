package com.aspd.backend.dto;

import lombok.*;

/**
 * Request DTO for creating or updating a baseline assignment.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BaselineAssignmentRequest {

    private Long studentDemandId;
    private Long plannedInternshipId;
    private String semester;
    private String schoolYear;
    private boolean pinned;
    private String notes;
}
