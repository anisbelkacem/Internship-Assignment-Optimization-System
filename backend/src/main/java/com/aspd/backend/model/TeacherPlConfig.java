package com.aspd.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(
        name = "teacher_pl_configs",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"teacher_id", "school_year"})
        }
)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TeacherPlConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pl_config_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", nullable = false)
    private Teacher teacher;

    // e.g. "2024/2025"
    @Column(name = "school_year", nullable = false, length = 16)
    private String schoolYear;

    @Column(name = "total_hours_credit")
    private Integer totalHoursCredit;

    @Enumerated(EnumType.STRING)
    @Column(name = "availability_status")
    private AvailabilityStatus availabilityStatus;

    // Subjects per config (per year)
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "teacher_pl_courses",
            joinColumns = @JoinColumn(name = "pl_config_id"),
            inverseJoinColumns = @JoinColumn(name = "course_id")
    )
    private Set<Course> subjectSpecializations = new HashSet<>();

    // Internship preferences per config (per year)
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "teacher_pl_internship_prefs",
            joinColumns = @JoinColumn(name = "pl_config_id")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "preference", nullable = false)
    private Set<PraktikumType> internshipPreferences = new HashSet<>();

    @Column(name = "max_praktika_per_year")
    private Integer maxPraktikaPerYear;
}
