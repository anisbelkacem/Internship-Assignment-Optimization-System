package com.aspd.backend.dto;

import com.aspd.backend.model.Course;
import com.aspd.backend.model.PraktikumType;

import java.util.Set;

public record TeacherPlConfigDto(
        Long id,
        String schoolYear,
        Integer maxPraktikaPerYear,
        Integer totalHoursCredit,
        Set<Course> subjectSpecializations,
        Set<PraktikumType> internshipPreferences
) {
}
