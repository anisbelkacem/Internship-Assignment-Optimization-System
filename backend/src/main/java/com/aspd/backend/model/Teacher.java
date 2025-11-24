package com.aspd.backend.model;

import jakarta.persistence.*;

import java.util.HashSet;
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
    @Enumerated(EnumType.STRING)
    private Course mainSubject; // main_subject (primary subject)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "school_id", nullable = false)
    private School school; // school_id (can be null for now until School is implemented)

    @Column(name = "email", nullable = false, unique = true)
    private String email; // email, main identifier for duplicates

    @OneToMany(
            mappedBy = "teacher",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private Set<TeacherPlConfig> plConfigs = new HashSet<>();

    public Set<TeacherPlConfig> getPlConfigs() {
        return plConfigs;
    }



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

    public Course getMainSubject() {
        return mainSubject;
    }

    public void setMainSubject(Course mainSubject) {
        this.mainSubject = mainSubject;
    }

    public School getSchool() {
        return school;
    }

    public void setSchool(School school) {
        this.school = school;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }


    public void addPlConfig(TeacherPlConfig cfg) {
        plConfigs.add(cfg);
        cfg.setTeacher(this);
    }

    public void removePlConfig(TeacherPlConfig cfg) {
        plConfigs.remove(cfg);
        cfg.setTeacher(null);
    }

}
