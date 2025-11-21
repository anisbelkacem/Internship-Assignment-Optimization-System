package com.aspd.backend.model;

import jakarta.persistence.*;
import java.util.Set;

@Entity
@Table(
        name = "teachers",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"email"})
        }
)
public class Teacher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "teacher_id")
    private Long teacherId; // from data model: teacher_id

    @Column(name = "first_name", nullable = false)
    private String firstName; // first_name

    @Column(name = "last_name", nullable = false)
    private String lastName; // last_name

    @Column(name = "main_subject", nullable = false)
    private String mainSubject; // main_subject (primary subject)

    @Column(name = "school_id")
    private Long schoolId; // school_id (can be null for now until School is implemented)

    @Column(name = "max_praktika_per_year")
    private Integer maxPraktikaPerYear; // workload info

    @Column(name = "email", nullable = false, unique = true)
    private String email; // email, main identifier for duplicates

    @Column(name = "total_hours_credit")
    private Integer totalHoursCredit; // total_hours_credit (reduction hours)

    @Enumerated(EnumType.STRING)
    @Column(name = "availability_status")
    private AvailabilityStatus availabilityStatus; // availability_status

    // ----- Extra fields for PL story -----

    // Multiple subject specializations (Math, Physics, etc.)
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "teacher_subjects",
            joinColumns = @JoinColumn(name = "teacher_id")
    )
    @Column(name = "subject", nullable = false)
    private Set<String> subjectSpecializations;

    // Internship preferences: PDP I, PDP II, ZSP, SFP
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "teacher_internship_preferences",
            joinColumns = @JoinColumn(name = "teacher_id")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "preference", nullable = false)
    private Set<PraktikumType> internshipPreferences;

    // ----- Getters & setters -----

    public Long getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(Long teacherId) {
        this.teacherId = teacherId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getMainSubject() {
        return mainSubject;
    }

    public void setMainSubject(String mainSubject) {
        this.mainSubject = mainSubject;
    }

    public Long getSchoolId() {
        return schoolId;
    }

    public void setSchoolId(Long schoolId) {
        this.schoolId = schoolId;
    }

    public Integer getMaxPraktikaPerYear() {
        return maxPraktikaPerYear;
    }

    public void setMaxPraktikaPerYear(Integer maxPraktikaPerYear) {
        this.maxPraktikaPerYear = maxPraktikaPerYear;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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
}
