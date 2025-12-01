package com.aspd.backend.dto;

import com.aspd.backend.model.Course;
import com.aspd.backend.model.SchoolType;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
public class StudentConfigDto {

    private Long id;
    private int studentId;
    private String year;

    @Enumerated(EnumType.STRING)
    private SchoolType schoolType;

    private Course mainCourse;
    private Course prefCourse1;
    private Course prefCourse2;
    private Course prefCourse3;

    private boolean pdpI;
    private boolean pdpII;
    private boolean zsp;
    private boolean sfp;

    public StudentConfigDto(Long id,
                            int studentId,
                            String year,
                            Course mainCourse,
                            Course prefCourse1,
                            Course prefCourse2,
                            Course prefCourse3,
                            SchoolType schoolType,
                            boolean pdpI,
                            boolean pdpII,
                            boolean zsp,
                            boolean sfp) {
        this.id = id;
        this.studentId = studentId;
        this.year = year;
        this.mainCourse = mainCourse;
        this.prefCourse1 = prefCourse1;
        this.prefCourse2 = prefCourse2;
        this.prefCourse3 = prefCourse3;
        this.schoolType = schoolType;
        this.pdpI = pdpI;
        this.pdpII = pdpII;
        this.zsp = zsp;
        this.sfp = sfp;
    }
}
