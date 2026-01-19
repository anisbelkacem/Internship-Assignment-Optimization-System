package com.aspd.backend.model;

import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.entity.PlanningPin;
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
 * Planning entity for the second optimization: assigning students to planned internships.
 * 
 * This represents a student's need for a specific internship type.
 * OptaPlanner will assign each demand to a suitable PlannedInternship.
 * 
 * Input (problem facts):
 * - studentConfig: Which student needs the internship
 * - praktikumType: What type they need (PDP_I, PDP_II, ZSP, SFP)
 * - preferredCourse: Student's course preference for this internship
 * 
 * Output (planning variable - what OptaPlanner decides):
 * - assignedInternship: Which PlannedInternship this student is assigned to
 */
@Entity
@Table(name = "student_internship_demands")
@PlanningEntity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentInternshipDemand {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // INPUT: Student's internship requirement
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "student_config_id", nullable = false)
    private StudentConfig studentConfig;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PraktikumType praktikumType;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "preferred_course_id")
    private Course preferredCourse; // Student's course preference for this internship

    @Column(name = "school_year", nullable = false)
    private String schoolYear;

    // RE-OPTIMIZATION SUPPORT
    
    /**
     * Whether this assignment should be pinned (not changed) during re-optimization.
     * When true, OptaPlanner will not modify the assignedInternship for this demand.
     * Used to preserve valid assignments from baseline when re-optimizing for semester changes.
     */
    @PlanningPin
    @Column(nullable = false)
    @Builder.Default
    private boolean pinned = false;

    // OUTPUT: What OptaPlanner assigns
    
    /**
     * The planned internship (with teacher + school) that this student is assigned to.
     * OptaPlanner will select from available PlannedInternships that match the type.
     */
    @PlanningVariable(
            valueRangeProviderRefs = "internshipRange",
            nullable = true
    )
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "assigned_internship_id")
    private PlannedInternship assignedInternship;

    // Helper method to get student's home address for distance calculations
    public Address getStudentAddress() {
        if (studentConfig != null && studentConfig.getStudent() != null) {
            return studentConfig.getStudent().getAddress();
        }
        return null;
    }
    
    // Helper method to get student's school type
    public SchoolType getStudentSchoolType() {
        if (studentConfig != null) {
            return studentConfig.getSchoolType();
        }
        return null;
    }
}
