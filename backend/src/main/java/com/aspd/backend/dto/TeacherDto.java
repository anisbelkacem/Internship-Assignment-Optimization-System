package com.aspd.backend.dto;

import com.aspd.backend.model.Course;

import java.util.List;

public record TeacherDto(
        Long teacherId,
        String firstName,
        String lastName,
        Course mainSubject,
        Long schoolId,
        String schoolName,
        String schoolZone,
        String email,
        List<TeacherPlConfigDto> plConfigs
) {
}