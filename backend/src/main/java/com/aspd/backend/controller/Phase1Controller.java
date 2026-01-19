package com.aspd.backend.controller;

import com.aspd.backend.dto.PlannedInternshipDto;
import com.aspd.backend.dto.TeacherAssignmentResult;
import com.aspd.backend.model.PlannedInternship;
import com.aspd.backend.model.School;
import com.aspd.backend.model.StudentConfig;
import com.aspd.backend.model.Teacher;
import com.aspd.backend.repository.PlannedInternshipRepository;
import com.aspd.backend.repository.SchoolRepository;
import com.aspd.backend.repository.StudentConfigRepository;
import com.aspd.backend.repository.TeacherRepository;
import com.aspd.backend.service.Phase1OptimizationService;
import com.aspd.backend.solver.InternshipSolution;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
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
    private final PlannedInternshipRepository plannedInternshipRepository;

    public Phase1Controller(
            Phase1OptimizationService phase1OptimizationService,
            StudentConfigRepository studentConfigRepository,
            TeacherRepository teacherRepository,
            SchoolRepository schoolRepository,
            PlannedInternshipRepository plannedInternshipRepository) {
        this.phase1OptimizationService = phase1OptimizationService;
        this.studentConfigRepository = studentConfigRepository;
        this.teacherRepository = teacherRepository;
        this.schoolRepository = schoolRepository;
        this.plannedInternshipRepository = plannedInternshipRepository;
    }

    /**
     * Phase 1: Optimize teacher and school assignments to internships.
     * This creates planned internships and assigns teachers/schools to them.
     */
    @PreAuthorize("hasAnyAuthority('EDIT')")
    @PostMapping("/optimize")
    public ResponseEntity<TeacherAssignmentResult> optimize(
            @RequestParam(name = "schoolYear") String schoolYear,
            @RequestParam(name = "budget") Integer budget) {
        log.info("Received Phase 1 optimization request for year: {} with budget: {}", schoolYear, budget);

        // Load data (filter for active teachers and schools only)
        List<StudentConfig> studentConfigs = studentConfigRepository.findByYear(schoolYear);
        List<Teacher> teachers = teacherRepository.findAllWithConfigs().stream()
                .filter(Teacher::isActive)
                .collect(Collectors.toList());
        List<School> schools = schoolRepository.findAll().stream()
                .filter(s -> Boolean.TRUE.equals(s.getActive()))
                .collect(Collectors.toList());

        if (studentConfigs.isEmpty()) {
            log.error("NO STUDENT CONFIGS FOUND FOR YEAR: {}", schoolYear);
            return ResponseEntity.badRequest().build();
        }

        // Delete existing planned internships for this school year to avoid duplicates
        List<PlannedInternship> existingInternships = plannedInternshipRepository.findBySchoolYear(schoolYear);
        if (!existingInternships.isEmpty()) {
            log.info("Deleting {} existing planned internships for year {}", existingInternships.size(), schoolYear);
            plannedInternshipRepository.deleteAll(existingInternships);
        }

        // Run Phase 1 optimization
        InternshipSolution phase1Solution = phase1OptimizationService.optimize(
                teachers, schools, studentConfigs, schoolYear, budget);

        // Save the planned internships to the database so Phase 2 can use them
        List<PlannedInternship> savedInternships = plannedInternshipRepository.saveAll(
                phase1Solution.getPlannedInternships());
        
        log.info("Saved {} planned internships to database", savedInternships.size());

        // Build response
        TeacherAssignmentResult result = TeacherAssignmentResult.builder()
                .schoolYear(schoolYear)
                .totalPlannedInternships(savedInternships.size())
                .assignedCount((int) savedInternships.stream()
                        .filter(i -> i.getAssignedTeacher() != null)
                        .count())
                .unassignedCount((int) savedInternships.stream()
                        .filter(i -> i.getAssignedTeacher() == null)
                        .count())
                .score(phase1Solution.getScore().toString())
                .plannedInternships(savedInternships.stream()
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
                .course(internship.getCourse() != null ? internship.getCourse().getName() : null)
                .maxCapacity(internship.getMaxCapacity())
                .teacherId(internship.getAssignedTeacher() != null ?
                        String.valueOf(internship.getAssignedTeacher().getTeacherId()) : null)
                .teacherName(internship.getAssignedTeacher() != null ?
                        internship.getAssignedTeacher().getFirstName() + " " +
                                internship.getAssignedTeacher().getLastName() : null)
                .schoolId(internship.getSchool() != null ?
                        internship.getSchool().getId() : null)
                .schoolName(internship.getSchool() != null ?
                        internship.getSchool().getName() : null)
                .schoolZone(internship.getSchool() != null ?
                        internship.getSchool().getZone() : null)
                .build();
    }
}
