package com.aspd.backend.controller;

import com.aspd.backend.dto.AssignmentDto;
import com.aspd.backend.dto.InternshipAssignmentUpdateRequest;
import com.aspd.backend.mapper.AssignmentMapper;
import com.aspd.backend.model.AssignmentStatus;
import com.aspd.backend.model.InternshipAssignment;
import com.aspd.backend.service.InternshipAssignmentService;
import com.aspd.backend.util.AssignmentExcelExporter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
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

import java.io.IOException;
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
    @DeleteMapping
    public ResponseEntity<Void> deleteAssignmentsBySchoolYear(
            @RequestParam String schoolYear) {
        
        int deletedCount = assignmentService.deleteBySchoolYear(schoolYear);
        
        if (deletedCount > 0) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Export internship assignments to Excel file.
     * Returns a complete Excel spreadsheet with all assignment details including embedded information
     * (student names, teacher names, school names, etc.).
     * 
     * @param schoolYear optional school year to filter assignments
     * @return Excel file as byte array with appropriate headers
     */
    @GetMapping("/export")
    public ResponseEntity<byte[]> exportAssignmentsToExcel(
            @RequestParam(required = false) String schoolYear) {
        
        try {
            List<InternshipAssignment> assignments;
            if (schoolYear != null && !schoolYear.isEmpty()) {
                assignments = assignmentService.getBySchoolYear(schoolYear);
                log.info("Exporting {} assignments for school year: {}", assignments.size(), schoolYear);
            } else {
                assignments = assignmentService.getAllAssignments();
                log.info("Exporting {} assignments (all years)", assignments.size());
            }
            
            byte[] excelFile = AssignmentExcelExporter.exportAssignmentsToExcel(assignments);
            
            String filename = schoolYear != null && !schoolYear.isEmpty() 
                    ? "assignments_" + schoolYear + ".xlsx"
                    : "assignments_all.xlsx";
            
            return ResponseEntity.ok()
                    .contentType(MediaType.valueOf("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .body(excelFile);
        } catch (IOException e) {
            log.error("Error exporting assignments to Excel", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}