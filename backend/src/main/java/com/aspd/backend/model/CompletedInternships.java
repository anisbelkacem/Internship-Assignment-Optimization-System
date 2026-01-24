package com.aspd.backend.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "completed_internships")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CompletedInternships {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", nullable = false)
    private Teacher teacher;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "school_id", nullable = false)
    private School school;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PraktikumType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    private String description;

    public CompletedInternships(Student student, Teacher teacher, School school, PraktikumType type,
                                Course course) {
        this.student = student;
        this.teacher = teacher;
        this.school = school;
        this.type = type;
        this.course = course;
    }

}
