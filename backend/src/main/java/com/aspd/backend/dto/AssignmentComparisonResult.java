package com.aspd.backend.dto;

import lombok.*;

import java.util.List;

/**
 * DTO for comparing assignment results between two semesters.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssignmentComparisonResult {
    
    private String baselineSemester;
    private String targetSemester;
    
    private int totalBaselineAssignments;
    private int totalTargetAssignments;
    
    private int assignmentsPreserved;
    private int assignmentsChanged;
    
    private double preservationRate;
    
    private List<AssignmentComparison> comparisons;
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AssignmentComparison {
        private String studentName;
        private String praktikumType;
        private boolean preserved;
        
        // Baseline (winter) assignment
        private String baselineTeacher;
        private String baselineSchool;
        private String baselineCourse;
        
        // Target (summer) assignment
        private String targetTeacher;
        private String targetSchool;
        private String targetCourse;
        
        private String changeReason;
    }
}
