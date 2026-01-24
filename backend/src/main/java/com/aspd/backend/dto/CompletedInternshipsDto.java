package com.aspd.backend.dto;

import com.aspd.backend.model.Course;
import com.aspd.backend.model.PraktikumType;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
public class CompletedInternshipsDto {

    private Long id;
    private int studentId;
    private Long teacherId;
    private Long schoolId;
    private PraktikumType type;
    private Course course;
    private String description;

    public CompletedInternshipsDto(Long id, int studentId, Long teacherId, Long schoolId, PraktikumType type,
                                   Course course, String description) {
        this.id = id;
        this.studentId = studentId;
        this.teacherId = teacherId;
        this.schoolId = schoolId;
        this.type = type;
        this.course = course;
        this.description = description;
    }
}
