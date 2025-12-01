package com.aspd.backend.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "student_config")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StudentConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Foreign key to Student
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_matriculation_nbr", nullable = false)
    private Student student;

    // Year stored as string
    private String year;

    // Identical copies of student's courses
    @Enumerated(EnumType.STRING)
    private Course mainCourse;

    @Enumerated(EnumType.STRING)
    private Course prefCourse1;

    @Enumerated(EnumType.STRING)
    private Course prefCourse2;

    @Enumerated(EnumType.STRING)
    private Course prefCourse3;
    @Enumerated(EnumType.STRING)
    private SchoolType schoolType;

    // PraktikumType booleans
    private boolean pdpI;
    private boolean pdpII;
    private boolean zsp;
    private boolean sfp;

    // Optional convenience constructor to auto-copy courses
    public StudentConfig(Student student, String year, boolean pdpI,boolean pdpII, boolean zsp,boolean sfp) {
        this.student = student;
        this.year = year;

        // Copy courses from the student
        this.mainCourse = student.getMainCourse();
        this.prefCourse1 = student.getPrefCourse1();
        this.prefCourse2 = student.getPrefCourse2();
        this.prefCourse3 = student.getPrefCourse3();
        this.schoolType = student.getSchoolType();
        // Praktikum defaults
        this.pdpI = pdpI;
        this.pdpII = pdpII;
        this.zsp = zsp;
        this.sfp = sfp;
    }
}
