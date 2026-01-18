package com.aspd.backend.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

/**
 * Problem fact representing the weighted distribution of ZSP course preferences.
 * 
 * Separate maps for GS and MS schools, where each map contains:
 * - Key: Course
 * - Value: Weighted preference sum (main=0.5, pref1=0.3, pref2=0.15, pref3=0.05)
 * 
 * Used by soft constraints to guide ZSP course assignments toward student preferences.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ZspCourseDistribution {
    
    /**
     * Course preference distribution for GS (Grundschule) students.
     * Map of Course -> weighted preference sum.
     */
    private Map<Course, Double> gsDistribution;
    
    /**
     * Course preference distribution for MS (Mittelschule) students.
     * Map of Course -> weighted preference sum.
     */
    private Map<Course, Double> msDistribution;
}
