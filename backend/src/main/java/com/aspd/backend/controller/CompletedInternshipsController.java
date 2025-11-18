package com.aspd.backend.controller;

import com.aspd.backend.dto.CompletedInternshipsDto;
import com.aspd.backend.model.CompletedInternships;
import com.aspd.backend.service.CompletedInternshipsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/completed-internships")
public class CompletedInternshipsController {

    private final CompletedInternshipsService completedInternshipsService;

    public CompletedInternshipsController(CompletedInternshipsService completedInternshipsService) {
        this.completedInternshipsService = completedInternshipsService;
    }

    @PostMapping
    public ResponseEntity<CompletedInternships> create(@RequestBody CompletedInternshipsDto dto) {
        CompletedInternships created = completedInternshipsService.createCompletedInternship(dto);
        return ResponseEntity.ok(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CompletedInternships> getById(@PathVariable Long id) {
        return completedInternshipsService.getCompletedInternshipById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<CompletedInternships>> getAll() {
        return ResponseEntity.ok(completedInternshipsService.getAllCompletedInternships());
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<CompletedInternships>> getByStudentId(@PathVariable int studentId) {
        return ResponseEntity.ok(completedInternshipsService.getInternshipsByStudentId(studentId));
    }

    @GetMapping("/teacher/{teacherId}")
    public ResponseEntity<List<CompletedInternships>> getByTeacherId(@PathVariable Long teacherId) {
        return ResponseEntity.ok(completedInternshipsService.getInternshipsByTeacherId(teacherId));
    }

    @GetMapping("/school/{schoolId}")
    public ResponseEntity<List<CompletedInternships>> getBySchoolId(@PathVariable Long schoolId) {
        return ResponseEntity.ok(completedInternshipsService.getInternshipsBySchoolId(schoolId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CompletedInternships> update(
            @PathVariable Long id,
            @RequestBody CompletedInternshipsDto dto) {

        CompletedInternships updated = completedInternshipsService.updateCompletedInternship(id, dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        completedInternshipsService.deleteCompletedInternship(id);
        return ResponseEntity.noContent().build();
    }
}
