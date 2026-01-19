package com.aspd.backend.controller;

import com.aspd.backend.dto.AssignmentDto;
import com.aspd.backend.dto.InternshipAssignmentUpdateRequest;
import com.aspd.backend.mapper.AssignmentMapper;
import com.aspd.backend.model.AssignmentStatus;
import com.aspd.backend.model.InternshipAssignment;
import com.aspd.backend.service.InternshipAssignmentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller for managing internship assignments (CRUD operations).
 */
@Slf4j
@RestController
@RequestMapping("/api/internship-assignments")
public class InternshipAssignmentController {

    private final InternshipAssignmentService assignmentService;
    private final AssignmentMapper assignmentMapper;

    public InternshipAssignmentController(
            InternshipAssignmentService assignmentService,
            AssignmentMapper assignmentMapper) {
        this.assignmentService = assignmentService;
        this.assignmentMapper = assignmentMapper;
    }

    /**
     * Get all internship assignments, optionally filtered by school year.
     */
    @PreAuthorize("hasAnyAuthority('VIEW')")
    @GetMapping
    public ResponseEntity<List<AssignmentDto>> getAllAssignments(
            @RequestParam(required = false) String schoolYear) {
        
        List<InternshipAssignment> assignments;
        if (schoolYear != null && !schoolYear.isEmpty()) {
            assignments = assignmentService.getBySchoolYear(schoolYear);
        } else {
            assignments = assignmentService.getAllAssignments();
        }
        
        List<AssignmentDto> dtos = assignments.stream()
                .map(assignmentMapper::toDto)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(dtos);
    }

    /**
     * Get a single internship assignment by ID.
     */
    @PreAuthorize("hasAnyAuthority('VIEW')")
    @GetMapping("/{id}")
    public ResponseEntity<AssignmentDto> getAssignmentById(@PathVariable Long id) {
        return assignmentService.getById(id)
                .map(assignmentMapper::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Update an internship assignment.
     */
    @PreAuthorize("hasAnyAuthority('EDIT')")
    @PutMapping("/{id}")
    public ResponseEntity<AssignmentDto> updateAssignment(
            @PathVariable Long id,
            @RequestBody InternshipAssignmentUpdateRequest req) {

        return assignmentService.updateByIds(id, req)
                .map(assignmentMapper::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Partially update an internship assignment (e.g., change status only).
     */
    @PreAuthorize("hasAnyAuthority('EDIT')")
    @PatchMapping("/{id}/status")
    public ResponseEntity<AssignmentDto> updateAssignmentStatus(
            @PathVariable Long id,
            @RequestParam AssignmentStatus status) {
        
        return assignmentService.updateStatus(id, status)
                .map(assignmentMapper::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Delete an internship assignment by ID.
     */
    @PreAuthorize("hasAnyAuthority('EDIT')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAssignment(@PathVariable Long id) {
        if (assignmentService.delete(id)) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Delete all assignments for a specific school year.
     */
    @PreAuthorize("hasAnyAuthority('EDIT')")
    @DeleteMapping
    public ResponseEntity<Void> deleteAssignmentsBySchoolYear(
            @RequestParam String schoolYear) {

        assignmentService.deleteBySchoolYear(schoolYear);
        return ResponseEntity.noContent().build();
    }
}