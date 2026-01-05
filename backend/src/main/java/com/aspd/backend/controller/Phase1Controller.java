package com.aspd.backend.controller;

import com.aspd.backend.dto.*;
import com.aspd.backend.model.*;
import com.aspd.backend.repository.SchoolRepository;
import com.aspd.backend.repository.StudentConfigRepository;
import com.aspd.backend.repository.TeacherRepository;
import com.aspd.backend.service.Phase1OptimizationService;
import com.aspd.backend.solver.InternshipSolution;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Controller for Phase 1 optimization: Teacher and School assignment to internships.
 */
@Slf4j
@RestController
@RequestMapping("/api/internships/phase1")
public class Phase1Controller {

    private final Phase1OptimizationService phase1OptimizationService;
    private final StudentConfigRepository studentConfigRepository;
    private final TeacherRepository teacherRepository;
    private final SchoolRepository schoolRepository;

    public Phase1Controller(
            Phase1OptimizationService phase1OptimizationService,
            StudentConfigRepository studentConfigRepository,
            TeacherRepository teacherRepository,
            SchoolRepository schoolRepository) {
        this.phase1OptimizationService = phase1OptimizationService;
        this.studentConfigRepository = studentConfigRepository;
        this.teacherRepository = teacherRepository;
        this.schoolRepository = schoolRepository;
    }

    /**
     * Phase 1: Optimize teacher and school assignments to internships.
     * This creates planned internships and assigns teachers/schools to them.
     */
    // @PreAuthorize("hasAuthority('MANAGE_USERS')")  // Temporarily disabled for testing
    @PostMapping("/optimize")
    public ResponseEntity<TeacherAssignmentResult> optimize(
            @RequestParam(name = "schoolYear") String schoolYear) {
        log.info("Received Phase 1 optimization request for year: {}", schoolYear);

        // Load data
        List<StudentConfig> studentConfigs = studentConfigRepository.findByYear(schoolYear);
        List<Teacher> teachers = teacherRepository.findAllWithConfigs();
        List<School> schools = schoolRepository.findAll();

        if (studentConfigs.isEmpty()) {
            log.error("NO STUDENT CONFIGS FOUND FOR YEAR: {}", schoolYear);
            return ResponseEntity.badRequest().build();
        }

        // Run Phase 1 optimization
        InternshipSolution phase1Solution = phase1OptimizationService.optimize(
                teachers, schools, studentConfigs, schoolYear, 70);

        // Build response
        TeacherAssignmentResult result = TeacherAssignmentResult.builder()
                .schoolYear(schoolYear)
                .totalPlannedInternships(phase1Solution.getPlannedInternships().size())
                .assignedCount((int) phase1Solution.getPlannedInternships().stream()
                        .filter(i -> i.getAssignedTeacher() != null)
                        .count())
                .unassignedCount((int) phase1Solution.getPlannedInternships().stream()
                        .filter(i -> i.getAssignedTeacher() == null)
                        .count())
                .score(phase1Solution.getScore().toString())
                .plannedInternships(phase1Solution.getPlannedInternships().stream()
                        .map(this::toPlannedInternshipDto)
                        .collect(Collectors.toList()))
                .build();

        log.info("Phase 1 complete: {}/{} internships assigned teachers",
                result.getAssignedCount(), result.getTotalPlannedInternships());

        return ResponseEntity.ok(result);
    }

    private PlannedInternshipDto toPlannedInternshipDto(PlannedInternship internship) {
        return PlannedInternshipDto.builder()
                .id(internship.getId())
                .praktikumType(internship.getPraktikumType().toString())
                .schoolType(internship.getSchoolType().toString())
                .course(internship.getCourse() != null ? internship.getCourse().toString() : null)
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
}
