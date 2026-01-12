package com.aspd.backend.controller;

import com.aspd.backend.validation.InternshipAssignmentValidationService;
import com.aspd.backend.validation.ValidationResult;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/validation/internship-assignments")
@RequiredArgsConstructor
public class InternshipAssignmentValidationController {

    private final InternshipAssignmentValidationService validationService;

    public static class UpdateValidationRequest {
        public Long assignmentId;
        public Long teacherId;
        public Long schoolId;
    }

    @PostMapping("/update")
    public ResponseEntity<ValidationResult> validateUpdate(@RequestBody UpdateValidationRequest req) {
        return ResponseEntity.ok(validationService.validateUpdate(req.assignmentId, req.teacherId, req.schoolId));
    }
}
