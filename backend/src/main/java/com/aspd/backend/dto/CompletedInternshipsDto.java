package com.aspd.backend.dto;

import com.aspd.backend.model.Course;
import java.time.LocalDate;
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
    private Course course;
    private LocalDate startDate;
    private LocalDate endDate;
    private String description;

    public CompletedInternshipsDto(Long id, int studentId, Long teacherId, Long schoolId,
                                   Course course, LocalDate startDate, LocalDate endDate, String description) {
        this.id = id;
        this.studentId = studentId;
        this.teacherId = teacherId;
        this.schoolId = schoolId;
        this.course = course;
        this.startDate = startDate;
        this.endDate = endDate;
        this.description = description;
    }
}
