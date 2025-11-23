package com.aspd.backend.dto;

import java.util.Set;
import com.aspd.backend.model.AvailabilityStatus;
import com.aspd.backend.model.PraktikumType;

public record TeacherDto(
        Long teacherId,
        String firstName,
        String lastName,
        String mainSubject,
        Long schoolId,
        Integer maxPraktikaPerYear,
        String email,
        Integer totalHoursCredit,
        AvailabilityStatus availabilityStatus,
        Set<String> subjectSpecializations,
        Set<PraktikumType> internshipPreferences
) {
}