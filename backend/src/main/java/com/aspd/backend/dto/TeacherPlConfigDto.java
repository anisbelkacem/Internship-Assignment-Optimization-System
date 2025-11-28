package com.aspd.backend.dto;

import com.aspd.backend.model.AvailabilityStatus;
import com.aspd.backend.model.PraktikumType;

import java.util.Set;

public record TeacherPlConfigDto(
        Long id,
        String schoolYear,
        Integer maxPraktikaPerYear,
        Integer totalHoursCredit,
        AvailabilityStatus availabilityStatus,
        Set<String> subjectSpecializations,
        Set<PraktikumType> internshipPreferences
) {
}
