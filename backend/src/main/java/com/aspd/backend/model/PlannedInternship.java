package com.aspd.backend.model;

import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.variable.PlanningVariable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

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

    /**
     * The course/subject for this internship.
     * - For SFP: Fixed (set from student's main course - must not change)
     * - For ZSP: Planning variable (OptaPlanner decides which course)
     * - For PDP_I/PDP_II: null (no course requirement)
     */
    @PlanningVariable(
            valueRangeProviderRefs = "courseRange",
            nullable = true
    )
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "course_id")
    private Course course;

    /**
     * Original course value for SFP internships (to ensure they don't change).
     * For ZSP and PDP, this is null.
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "original_course_id")
    private Course originalCourse;

    @Column(name = "school_year", nullable = false)
    private String schoolYear; // e.g., "2025"

    // Capacity tracking (how many students can be assigned to this internship)
    @Column(nullable = false)
    private int maxCapacity; // 2 for PDP, 4 for ZSP/SFP
    
    @Column(nullable = false)
    private int currentAssignments = 0; // How many students assigned so far

    // OUTPUT: What OptaPlanner assigns
    
    /**
     * Whether this internship slot is active (opened).
     * OptaPlanner decides if we need this slot or not.
     * Active slots must have a teacher and school assigned.
     * Inactive slots don't consume from the total internship budget.
     * Uses Boolean (wrapper) not boolean (primitive) to allow OptaPlanner to represent uninitialized state.
     */
    @PlanningVariable(
            valueRangeProviderRefs = "booleanRange",
            nullable = false
    )
    @Column(nullable = true, columnDefinition = "BOOLEAN DEFAULT false")
    @lombok.Builder.Default
    private Boolean active = false;

    /**
     * The teacher assigned to supervise this internship.
     * OptaPlanner will select from the available teachers.
     * Only relevant if active=true.
     */
    @PlanningVariable(
            valueRangeProviderRefs = "teacherRange",
            nullable = true
    )
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "assigned_teacher_id")
    private Teacher assignedTeacher;


    // Helper methods
        /**
         * Get the school for this internship from the assigned teacher.
         * Teachers belong to exactly one school, so school is derived from teacher.
         */
        public School getSchool() {
            return assignedTeacher != null ? assignedTeacher.getSchool() : null;
        }
    
    
    public boolean isFull() {
        return currentAssignments >= maxCapacity;
    }
    
    public boolean hasCapacity() {
        return currentAssignments < maxCapacity;
    }
    
    public boolean isActive() {
        return active != null && active;
    }
    
    public int getRemainingCapacity() {
        return maxCapacity - currentAssignments;
    }
}
