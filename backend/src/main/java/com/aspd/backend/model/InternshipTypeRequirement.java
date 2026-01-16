package com.aspd.backend.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Problem fact representing a minimum activation requirement for a type/school/course combination.
 * Avoids groupBy caching issues by pre-calculating totals.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InternshipTypeRequirement {
    private PraktikumType type;
    private SchoolType schoolType;
    private Course course; // null for non-SFP types
    
    private int totalSlots;        // Total slots of this type/school/course
    private int requiredActive;    // Minimum required active
    
    public String getKey() {
        if (course != null) {
            return type + "/" + schoolType + "/" + course.getName();
        }
        return type + "/" + schoolType;
    }
    
    @Override
    public String toString() {
        if (course != null) {
            return type + " " + schoolType + "/" + course.getName() + " (req: " + requiredActive + "/" + totalSlots + ")";
        }
        return type + " " + schoolType + " (req: " + requiredActive + "/" + totalSlots + ")";
    }
}
