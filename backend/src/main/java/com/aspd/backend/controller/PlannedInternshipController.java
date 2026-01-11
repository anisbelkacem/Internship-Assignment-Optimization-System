package com.aspd.backend.controller;

import com.aspd.backend.dto.PlannedInternshipDto;
import com.aspd.backend.model.PlannedInternship;
import com.aspd.backend.service.PlannedInternshipService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller for managing saved PlannedInternship assignments.
 */
@Slf4j
@RestController
@RequestMapping("/api/planned-internships")
@RequiredArgsConstructor
public class PlannedInternshipController {

    private final PlannedInternshipService plannedInternshipService;

    /**
     * Get all planned internships for a specific school year.
     */
    @GetMapping
    public ResponseEntity<List<PlannedInternshipDto>> getBySchoolYear(
            @RequestParam(name = "schoolYear") String schoolYear) {
        log.info("GET /api/planned-internships?schoolYear={}", schoolYear);
        
        List<PlannedInternship> internships = plannedInternshipService.getBySchoolYear(schoolYear);
        List<PlannedInternshipDto> dtos = internships.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(dtos);
    }

    /**
     * Get a specific planned internship by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<PlannedInternshipDto> getById(@PathVariable Long id) {
        log.info("GET /api/planned-internships/{}", id);
        
        return plannedInternshipService.getById(id)
                .map(this::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Update a planned internship (change assigned teacher or school).
     */
    @PutMapping("/{id}")
    public ResponseEntity<PlannedInternshipDto> update(
            @PathVariable Long id,
            @RequestBody UpdatePlannedInternshipRequest request) {
        log.info("PUT /api/planned-internships/{} with teacher={}, school={}", 
                id, request.teacherId, request.schoolId);
        
        try {
            PlannedInternship updated = plannedInternshipService.update(
                    id, request.teacherId, request.schoolId);
            return ResponseEntity.ok(toDto(updated));
        } catch (RuntimeException e) {
            log.error("Failed to update planned internship: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Delete a planned internship by ID.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.info("DELETE /api/planned-internships/{}", id);
        plannedInternshipService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Delete all planned internships for a specific school year.
     */
    @DeleteMapping
    public ResponseEntity<Void> deleteBySchoolYear(
            @RequestParam(name = "schoolYear") String schoolYear) {
        log.info("DELETE /api/planned-internships?schoolYear={}", schoolYear);
        plannedInternshipService.deleteBySchoolYear(schoolYear);
        return ResponseEntity.noContent().build();
    }

    private PlannedInternshipDto toDto(PlannedInternship internship) {
        return PlannedInternshipDto.builder()
                .id(internship.getId())
                .praktikumType(internship.getPraktikumType().toString())
                .schoolType(internship.getSchoolType().toString())
                .course(internship.getCourse() != null ? internship.getCourse().getName() : null)
                .maxCapacity(internship.getMaxCapacity())
                .teacherId(internship.getAssignedTeacher() != null ?
                        String.valueOf(internship.getAssignedTeacher().getTeacherId()) : null)
                .teacherName(internship.getAssignedTeacher() != null ?
                        internship.getAssignedTeacher().getFirstName() + " " +
                                internship.getAssignedTeacher().getLastName() : null)
                .schoolId(internship.getAssignedSchool() != null ?
                        internship.getAssignedSchool().getId() : null)
                .schoolName(internship.getAssignedSchool() != null ?
                        internship.getAssignedSchool().getName() : null)
                .schoolZone(internship.getAssignedSchool() != null ?
                        internship.getAssignedSchool().getZone() : null)
                .build();
    }

    /**
     * Request DTO for updating a planned internship.
     */
    public static class UpdatePlannedInternshipRequest {
        public Long teacherId;
        public Long schoolId;
    }
}
