package com.aspd.backend.validation;

import lombok.Getter;

@Getter
public class AssignmentValidationException extends RuntimeException {
    private final ValidationResult result;

    public AssignmentValidationException(ValidationResult result) {
        super("Assignment violates hard constraints");
        this.result = result;
    }
}
