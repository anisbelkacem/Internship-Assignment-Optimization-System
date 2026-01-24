package com.aspd.backend.model;

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
 * Represents the final assignment of a student to a planned internship.
 * This is NOT a planning entity - it's the result after OptaPlanner assigns
 * students to PlannedInternship slots.
 *
 * Flow:
 * 1. OptaPlanner creates PlannedInternships (teacher + school + type)
 * 2. Students are matched to PlannedInternships
 * 3. InternshipAssignments are created to record the matches
 */
@Entity
@Table(name = "internship_assignments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InternshipAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Reference to the student's configuration (which praktikum types they need)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "student_config_id", nullable = false)
    private StudentConfig studentConfig;

    // Reference to the planned internship this student is assigned to
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "planned_internship_id", nullable = false)
    private PlannedInternship plannedInternship;

    // Denormalized data for quick access (copied from PlannedInternship)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "teacher_id")
    private Teacher teacher;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "school_id")
    private School school;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PraktikumType praktikumType;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "course_id")
    private Course course; // The subject being taught (for ZSP/SFP)

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AssignmentStatus status = AssignmentStatus.PROPOSED;

    @Column(name = "school_year")
    private String schoolYear;
}

