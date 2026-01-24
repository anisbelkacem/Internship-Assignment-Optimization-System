package com.aspd.backend.dto;

import com.aspd.backend.model.Course;
import com.aspd.backend.model.PraktikumType;

import java.util.Set;

public record TeacherPlConfigRequest(
        String schoolYear,
        Integer maxPraktikaPerYear,
        Integer totalHoursCredit,
        Set<Long> subjectSpecializations,
        Set<PraktikumType> internshipPreferences
) {
}
