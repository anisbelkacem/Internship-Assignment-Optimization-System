package com.aspd.backend.controller;

import com.aspd.backend.validation.PlannedInternshipValidationService;
import com.aspd.backend.validation.ValidationResult;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/validation")
@RequiredArgsConstructor
public class ValidationController {

    private final PlannedInternshipValidationService plannedInternshipValidationService;

    public static class PlannedInternshipUpdateValidationRequest {
        public Long internshipId;
        public Long teacherId;
        public Long schoolId;
    }

    @PostMapping("/planned-internships/update")
    public ResponseEntity<ValidationResult> validatePlannedInternshipUpdate(
            @RequestBody PlannedInternshipUpdateValidationRequest req) {

        ValidationResult result = plannedInternshipValidationService
                .validatePlannedInternshipUpdate(req.internshipId, req.teacherId, req.schoolId);

        return ResponseEntity.ok(result);
    }
}
