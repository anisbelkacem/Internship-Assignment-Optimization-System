package com.aspd.backend.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

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

    @Column(nullable = false)
    private Long teacherId;

    @Column(nullable = false)
    private Long schoolId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Course course;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    private String description;

    public CompletedInternships(Student student, Long teacherId, Long schoolId,
                                Course course, LocalDate startDate, LocalDate endDate) {
        this.student = student;
        this.teacherId = teacherId;
        this.schoolId = schoolId;
        this.course = course;
        this.startDate = startDate;
        this.endDate = endDate;
    }
}
