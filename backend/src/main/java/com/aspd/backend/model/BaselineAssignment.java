package com.aspd.backend.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Represents a snapshot of a student-internship assignment at a specific point in time.
 * Used as a baseline for re-optimization to preserve valid assignments and minimize disruption.
 * 
 * When re-optimizing for a new semester (e.g., from winter to summer), we capture the current
 * state as a baseline. OptaPlanner then tries to preserve these assignments where possible,
 * only changing them if necessary to accommodate new requirements or configurations.
 */
@Entity
@Table(name = "baseline_assignments", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"student_demand_id", "school_year", "semester"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BaselineAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The student's internship demand that was assigned.
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "student_demand_id", nullable = false)
    private StudentInternshipDemand studentDemand;

    /**
     * The planned internship this student was assigned to in the baseline.
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "planned_internship_id", nullable = false)
    private PlannedInternship plannedInternship;

    /**
     * The teacher who was assigned to this internship in the baseline.
     * Stored separately to detect if teacher changes even if internship changes.
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "teacher_id", nullable = false)
    private Teacher teacher;

    /**
     * The school where this internship takes place in the baseline.
     * Stored separately to detect if school changes even if teacher stays same.
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "school_id", nullable = false)
    private School school;

    /**
     * Which semester this baseline represents (e.g., "winter", "summer")
     */
    @Column(nullable = false, length = 20)
    private String semester;

    /**
     * School year for this baseline (e.g., "2025", "WiSe2026")
     */
    @Column(name = "school_year", nullable = false, length = 50)
    private String schoolYear;

    /**
     * When this baseline assignment was captured/created.
     */
    @Column(nullable = false)
    private LocalDateTime capturedAt;

    /**
     * Whether this assignment should be pinned during re-optimization.
     * Pinned assignments will not be changed by OptaPlanner.
     * Use this for assignments that must remain fixed (e.g., already completed).
     */
    @Column(nullable = false)
    private boolean pinned;

    /**
     * Optional notes about why this baseline was created or any special considerations.
     */
    @Column(length = 500)
    private String notes;

    /**
     * Who created this baseline (for audit purposes).
     */
    @Column(length = 100)
    private String createdBy;
}
