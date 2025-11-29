package com.aspd.backend.model;

import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(
        name = "teacher_pl_configs",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"teacher_id", "school_year"})
        }
)
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
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "teacher_pl_subjects",
            joinColumns = @JoinColumn(name = "pl_config_id")
    )
    @Column(name = "subject", nullable = false)
    private Set<String> subjectSpecializations = new HashSet<>();

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

    // --- Getters & setters ---

    public Long getId() {
        return id;
    }

    public Teacher getTeacher() {
        return teacher;
    }

    public void setTeacher(Teacher teacher) {
        this.teacher = teacher;
    }

    public String getSchoolYear() {
        return schoolYear;
    }

    public void setSchoolYear(String schoolYear) {
        this.schoolYear = schoolYear;
    }

    public Integer getTotalHoursCredit() {
        return totalHoursCredit;
    }

    public void setTotalHoursCredit(Integer totalHoursCredit) {
        this.totalHoursCredit = totalHoursCredit;
    }

    public AvailabilityStatus getAvailabilityStatus() {
        return availabilityStatus;
    }

    public void setAvailabilityStatus(AvailabilityStatus availabilityStatus) {
        this.availabilityStatus = availabilityStatus;
    }

    public Set<String> getSubjectSpecializations() {
        return subjectSpecializations;
    }

    public void setSubjectSpecializations(Set<String> subjectSpecializations) {
        this.subjectSpecializations = subjectSpecializations;
    }

    public Set<PraktikumType> getInternshipPreferences() {
        return internshipPreferences;
    }

    public void setInternshipPreferences(Set<PraktikumType> internshipPreferences) {
        this.internshipPreferences = internshipPreferences;
    }

    public Integer getMaxPraktikaPerYear() {
        return maxPraktikaPerYear;
    }

    public void setMaxPraktikaPerYear(Integer maxPraktikaPerYear) {
        this.maxPraktikaPerYear = maxPraktikaPerYear;
    }
}
