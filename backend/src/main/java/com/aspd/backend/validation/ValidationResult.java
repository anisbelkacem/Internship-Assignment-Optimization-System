package com.aspd.backend.validation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class ValidationResult {
    private boolean hardValid;
    private List<ValidationViolation> hardViolations;
    private List<ValidationViolation> warnings;

    public static ValidationResult ok() {
        return ValidationResult.builder()
                .hardValid(true)
                .hardViolations(List.of())
                .warnings(List.of())
                .build();
    }
}
