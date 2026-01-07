package com.aspd.backend.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Result DTO for Phase 2 optimization (Student assignment to internships).
 */
@Data
@Builder
public class StudentAssignmentResult {
    private String schoolYear;
    private int totalStudents;
    private int assignedStudents;
    private int unassignedStudents;
    private String score; // OptaPlanner score from Phase 2
    private List<AssignmentDto> assignments;
}
