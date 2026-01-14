package com.aspd.backend.validation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class ValidationViolation {
    private String code;                 // e.g. "ZONE_VIOLATION"
    private ViolationSeverity severity;  // HARD/WARNING
    private String message;              // message shown in UI
    private List<String> fields;         // ["teacherId"], ["schoolId"], ...
}
