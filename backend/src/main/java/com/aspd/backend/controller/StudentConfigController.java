package com.aspd.backend.controller;

import com.aspd.backend.dto.StudentConfigDto;
import com.aspd.backend.service.StudentConfigService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/student-configs")
public class StudentConfigController {

    private final StudentConfigService configService;

    public StudentConfigController(StudentConfigService configService) {
        this.configService = configService;
    }
    @PreAuthorize("hasAuthority('VIEW') or hasAnyAuthority('EDIT')")
    @GetMapping("/year/{year}")
    public ResponseEntity<List<StudentConfigDto>> getConfigsByYear(@PathVariable String year) {
        List<StudentConfigDto> dtos = configService.getConfigsByYear(year);
        return ResponseEntity.ok(dtos);
    }


    @PreAuthorize("hasAuthority('VIEW') or hasAnyAuthority('EDIT')")
    @GetMapping
    public ResponseEntity<List<StudentConfigDto>> getAllConfigs() {
        List<StudentConfigDto> dtos = configService.getAllConfigs();
        return ResponseEntity.ok(dtos);
    }


    @PreAuthorize("hasAuthority('VIEW') or hasAnyAuthority('EDIT')")
    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<StudentConfigDto>> getConfigsByStudent(@PathVariable int studentId) {
        List<StudentConfigDto> dtos = configService.getConfigsByStudent(studentId);
        return ResponseEntity.ok(dtos);
    }

    @PreAuthorize("hasAuthority('VIEW') or hasAnyAuthority('EDIT')")
    @GetMapping("/student/{studentId}/year/{year}")
    public ResponseEntity<StudentConfigDto> getConfig(@PathVariable int studentId, @PathVariable String year) {
        StudentConfigDto dto = configService.getConfig(studentId, year);
        return ResponseEntity.ok(dto);
    }
    @PreAuthorize("hasAnyAuthority('EDIT')")
    @PostMapping
    public ResponseEntity<StudentConfigDto> createConfig(@RequestBody StudentConfigDto dto) {
        StudentConfigDto created = configService.createConfig(dto);
        return ResponseEntity.ok(created);
    }


    @PreAuthorize("hasAnyAuthority('EDIT')")
    @PutMapping("/{id}")
    public ResponseEntity<StudentConfigDto> updateConfig(@PathVariable Long id, @RequestBody StudentConfigDto dto) {
        StudentConfigDto updated = configService.updateConfig(id, dto);
        return ResponseEntity.ok(updated);
    }

    @PreAuthorize("hasAnyAuthority('EDIT')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteConfig(@PathVariable Long id) {
        configService.deleteConfig(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAuthority('VIEW') or hasAnyAuthority('EDIT')")
    @GetMapping("/years")
    public ResponseEntity<List<String>> getAllYears() {
        List<String> years = configService.getAllYears();
        return ResponseEntity.ok(years);
    }

}
