package com.aspd.backend.dto;

import com.aspd.backend.model.Course;
import com.aspd.backend.model.School;

public record TeacherRequest(
        String firstName,
        String lastName,
        Course mainSubject,
        School schoolId,
        String email
) {
}