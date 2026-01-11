package com.aspd.backend.controller;

import com.aspd.backend.dto.AssignmentDto;
import com.aspd.backend.model.AssignmentStatus;
import com.aspd.backend.model.InternshipAssignment;
import com.aspd.backend.repository.InternshipAssignmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
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
@RequiredArgsConstructor
public class InternshipAssignmentController {

    private final InternshipAssignmentRepository assignmentRepository;

    /**
     * Get all internship assignments, optionally filtered by school year.
     */
    @GetMapping
    public ResponseEntity<List<AssignmentDto>> getAllAssignments(
            @RequestParam(required = false) String schoolYear) {
        
        List<InternshipAssignment> assignments;
        
        if (schoolYear != null && !schoolYear.isEmpty()) {
            log.info("Fetching assignments for school year: {}", schoolYear);
            assignments = assignmentRepository.findBySchoolYear(schoolYear);
        } else {
            log.info("Fetching all assignments");
            assignments = assignmentRepository.findAll();
        }
        
        List<AssignmentDto> dtos = assignments.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(dtos);
    }

    /**
     * Get a single internship assignment by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<AssignmentDto> getAssignmentById(@PathVariable Long id) {
        log.info("Fetching assignment with ID: {}", id);
        
        return assignmentRepository.findById(id)
                .map(this::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Update an internship assignment.
     */
    @PutMapping("/{id}")
    public ResponseEntity<AssignmentDto> updateAssignment(
            @PathVariable Long id,
            @RequestBody InternshipAssignment updatedAssignment) {
        
        log.info("Updating assignment with ID: {}", id);
        
        return assignmentRepository.findById(id)
                .map(existing -> {
                    // Update fields
                    if (updatedAssignment.getStartDate() != null) {
                        existing.setStartDate(updatedAssignment.getStartDate());
                    }
                    if (updatedAssignment.getEndDate() != null) {
                        existing.setEndDate(updatedAssignment.getEndDate());
                    }
                    if (updatedAssignment.getStatus() != null) {
                        existing.setStatus(updatedAssignment.getStatus());
                    }
                    
                    InternshipAssignment saved = assignmentRepository.save(existing);
                    return ResponseEntity.ok(toDto(saved));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Partially update an internship assignment (e.g., change status only).
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<AssignmentDto> updateAssignmentStatus(
            @PathVariable Long id,
            @RequestParam AssignmentStatus status) {
        
        log.info("Updating assignment {} status to: {}", id, status);
        
        return assignmentRepository.findById(id)
                .map(assignment -> {
                    assignment.setStatus(status);
                    InternshipAssignment saved = assignmentRepository.save(assignment);
                    return ResponseEntity.ok(toDto(saved));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Delete an internship assignment by ID.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAssignment(@PathVariable Long id) {
        log.info("Deleting assignment with ID: {}", id);
        
        if (!assignmentRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        
        assignmentRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Delete all assignments for a specific school year.
     */
    @DeleteMapping
    public ResponseEntity<Void> deleteAssignmentsBySchoolYear(
            @RequestParam String schoolYear) {
        
        log.info("Deleting all assignments for school year: {}", schoolYear);
        
        List<InternshipAssignment> assignments = assignmentRepository.findBySchoolYear(schoolYear);
        
        if (assignments.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        assignmentRepository.deleteAll(assignments);
        log.info("Deleted {} assignments for school year {}", assignments.size(), schoolYear);
        
        return ResponseEntity.noContent().build();
    }

    private AssignmentDto toDto(InternshipAssignment assignment) {
        AssignmentDto dto = new AssignmentDto();
        dto.setId(assignment.getId());
        dto.setStudentName(assignment.getStudentConfig().getStudent().getFirstName() + " " +
                assignment.getStudentConfig().getStudent().getLastName());
        dto.setPraktikumType(assignment.getPraktikumType().toString());
        
        if (assignment.getCourse() != null) {
            dto.setCourse(assignment.getCourse().getName());
        }
        
        if (assignment.getTeacher() != null) {
            dto.setTeacherName(assignment.getTeacher().getFirstName() + " " +
                    assignment.getTeacher().getLastName());
        }
        
        if (assignment.getSchool() != null) {
            dto.setSchoolName(assignment.getSchool().getName());
        }
        
        dto.setStatus(assignment.getStatus().toString());
        
        return dto;
    }
}
