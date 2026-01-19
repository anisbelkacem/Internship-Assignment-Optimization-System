package com.aspd.backend.dto;

import lombok.*;
import java.time.LocalDateTime;

/**
 * DTO for transferring baseline assignment data to/from frontend.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BaselineAssignmentDto {

    private Long id;
    
    // Student demand information
    private Long studentDemandId;
    private int studentMatriculationNbr;
    private String studentName;
    private String praktikumType;
    
    // Assignment details
    private Long plannedInternshipId;
    private Long teacherId;
    private String teacherName;
    private Long schoolId;
    private String schoolName;
    
    // Baseline metadata
    private String semester;
    private String schoolYear;
    private LocalDateTime capturedAt;
    private boolean pinned;
    private String notes;
    private String createdBy;
}
