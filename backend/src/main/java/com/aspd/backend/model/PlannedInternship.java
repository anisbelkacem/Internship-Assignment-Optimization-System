package com.aspd.backend.model;

import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.variable.PlanningVariable;
import lombok.*;
import jakarta.persistence.*;

/**
 * Planning entity for the first optimization: assigning teachers to internships.
 * 
 * This represents a "slot" for an internship that needs to be staffed.
 * OptaPlanner will assign a teacher and school to each planned internship.
 * 
 * Input (problem facts):
 * - praktikumType: What type of internship (PDP_I, PDP_II, ZSP, SFP)
 * - schoolType: Required school type (GRUNDSCHULE or MITTELSCHULE)
 * - course: Required subject (for ZSP/SFP only, null for PDP)
 * - schoolYear: Which academic year this belongs to
 * 
 * Output (planning variables - what OptaPlanner decides):
 * - assignedTeacher: Which teacher will supervise this internship
 * - assignedSchool: Which school will host this internship
 */
@Entity
@Table(name = "planned_internships")
@PlanningEntity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlannedInternship {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // INPUT: What kind of internship is needed
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PraktikumType praktikumType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SchoolType schoolType; // GRUNDSCHULE or MITTELSCHULE

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "course_id")
    private Course course; // Required for ZSP/SFP, null for PDP_I/PDP_II

    @Column(name = "school_year", nullable = false)
    private String schoolYear; // e.g., "2025"

    // Capacity tracking (how many students can be assigned to this internship)
    @Column(nullable = false)
    private int maxCapacity; // 2 for PDP, 4 for ZSP/SFP
    
    @Column(nullable = false)
    private int currentAssignments = 0; // How many students assigned so far

    // OUTPUT: What OptaPlanner assigns
    
    /**
     * The teacher assigned to supervise this internship.
     * OptaPlanner will select from the available teachers.
     */
    @PlanningVariable(
            valueRangeProviderRefs = "teacherRange",
            nullable = true
    )
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "assigned_teacher_id")
    private Teacher assignedTeacher;

    /**
     * The school where this internship will take place.
     * OptaPlanner will select from the available schools.
     */
    @PlanningVariable(
            valueRangeProviderRefs = "schoolRange",
            nullable = true
    )
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "assigned_school_id")
    private School assignedSchool;

    // Helper methods
    
    public boolean isFull() {
        return currentAssignments >= maxCapacity;
    }
    
    public boolean hasCapacity() {
        return currentAssignments < maxCapacity;
    }
    
    public int getRemainingCapacity() {
        return maxCapacity - currentAssignments;
    }
}
