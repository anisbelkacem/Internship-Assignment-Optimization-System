package com.aspd.backend.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Result DTO for Phase 1 optimization (Teacher & School assignment to internships).
 */
@Data
@Builder
public class TeacherAssignmentResult {
    private String schoolYear;
    private int totalPlannedInternships;
    private int assignedCount; // Internships with assigned teachers
    private int unassignedCount; // Internships without teachers
    private String score; // OptaPlanner score from Phase 1
    private List<PlannedInternshipDto> plannedInternships;
}
