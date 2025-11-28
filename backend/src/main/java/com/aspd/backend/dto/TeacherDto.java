package com.aspd.backend.dto;

import com.aspd.backend.model.Course;
import com.aspd.backend.model.School;

import java.util.List;

public record TeacherDto(
        Long teacherId,
        String firstName,
        String lastName,
        Course mainSubject,
        School school,
        String email,
        List<TeacherPlConfigDto> plConfigs
) {
}