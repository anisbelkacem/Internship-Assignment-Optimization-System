package com.aspd.backend.dto;

import lombok.Builder;
import lombok.Data;

/**
 * DTO for PlannedInternship entity (Phase 1 output).
 */
@Data
@Builder
public class PlannedInternshipDto {
    private Long id;
    private String praktikumType;
    private String schoolType;
    private String course;
    private int maxCapacity;
    private String teacherId;
    private String teacherName;
    private Long schoolId;
    private String schoolName;
    private String schoolZone;
}
