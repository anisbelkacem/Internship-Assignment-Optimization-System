package com.aspd.backend.controller;

import com.aspd.backend.dto.StudentConfigDto;
import com.aspd.backend.validation.StudentConfigValidationService;
import com.aspd.backend.validation.ValidationResult;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/validation/student-config")
@RequiredArgsConstructor
public class StudentConfigValidationController {

    private final StudentConfigValidationService validationService;

    @PostMapping
    public ResponseEntity<ValidationResult> validate(@RequestBody StudentConfigDto dto) {
        return ResponseEntity.ok(validationService.validate(dto));
    }
}
