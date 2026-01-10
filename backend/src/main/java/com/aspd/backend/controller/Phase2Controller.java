package com.aspd.backend.controller;

import com.aspd.backend.dto.AssignmentDto;
import com.aspd.backend.dto.StudentAssignmentResult;
import com.aspd.backend.model.InternshipAssignment;
import com.aspd.backend.model.StudentConfig;
import com.aspd.backend.repository.InternshipAssignmentRepository;
import com.aspd.backend.repository.StudentConfigRepository;
import com.aspd.backend.service.Phase2OptimizationService;
import com.aspd.backend.solver.StudentAssignmentSolution;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller for Phase 2 optimization: Student assignment to internships.
 */
@Slf4j
@RestController
@RequestMapping("/api/internships/phase2")
public class Phase2Controller {

    private final Phase2OptimizationService phase2OptimizationService;
    private final StudentConfigRepository studentConfigRepository;
    private final InternshipAssignmentRepository assignmentRepository;

    public Phase2Controller(
            Phase2OptimizationService phase2OptimizationService,
            StudentConfigRepository studentConfigRepository,
            InternshipAssignmentRepository assignmentRepository) {
        this.phase2OptimizationService = phase2OptimizationService;
        this.studentConfigRepository = studentConfigRepository;
        this.assignmentRepository = assignmentRepository;
    }

    /**
     * Phase 2: Optimize student assignments to internships.
     * This assigns students to the planned internships created in Phase 1.
     */
    // @PreAuthorize("hasAuthority('MANAGE_USERS')")  // Temporarily disabled for testing
    @PostMapping("/optimize")
    public ResponseEntity<StudentAssignmentResult> optimize(
            @RequestParam(name = "schoolYear") String schoolYear) {
        log.info("Received Phase 2 optimization request for year: {}", schoolYear);

        // Load data
        List<StudentConfig> studentConfigs = studentConfigRepository.findByYear(schoolYear);

        if (studentConfigs.isEmpty()) {
            log.error("NO STUDENT CONFIGS FOUND FOR YEAR: {}", schoolYear);
            return ResponseEntity.badRequest().build();
        }

        // Delete existing assignments for this school year to avoid duplicates
        List<InternshipAssignment> existingAssignments = assignmentRepository.findBySchoolYear(schoolYear);
        if (!existingAssignments.isEmpty()) {
            log.info("Deleting {} existing assignments for year {}", existingAssignments.size(), schoolYear);
            assignmentRepository.deleteAll(existingAssignments);
        }

        // Run Phase 2 optimization
        StudentAssignmentSolution phase2Solution = phase2OptimizationService.optimize(
                studentConfigs, schoolYear, 70);

        // Create and save final assignments
        List<InternshipAssignment> finalAssignments = phase2OptimizationService.createFinalAssignments(
                phase2Solution.getStudentDemands(), schoolYear);

        assignmentRepository.saveAll(finalAssignments);

        // Build response
        StudentAssignmentResult result = StudentAssignmentResult.builder()
                .schoolYear(schoolYear)
                .totalStudents(phase2Solution.getStudentDemands().size())
                .assignedStudents((int) phase2Solution.getStudentDemands().stream()
                        .filter(d -> d.getAssignedInternship() != null)
                        .count())
                .unassignedStudents((int) phase2Solution.getStudentDemands().stream()
                        .filter(d -> d.getAssignedInternship() == null)
                        .count())
                .score(phase2Solution.getScore().toString())
                .assignments(finalAssignments.stream()
                        .map(this::toDto)
                        .collect(Collectors.toList()))
                .build();

        log.info("Phase 2 complete: {}/{} students assigned",
                result.getAssignedStudents(), result.getTotalStudents());

        return ResponseEntity.ok(result);
    }

    private AssignmentDto toDto(InternshipAssignment assignment) {
        AssignmentDto dto = new AssignmentDto();
        dto.setId(assignment.getId());
        dto.setStudentName(assignment.getStudentConfig().getStudent().getFirstName() + " " +
                assignment.getStudentConfig().getStudent().getLastName());
        dto.setPraktikumType(assignment.getPraktikumType().toString());
        
        // Course comes from the assignment
        if (assignment.getCourse() != null) {
            dto.setCourse(assignment.getCourse().toString());
        }

        // Teacher and school come from the denormalized fields
        if (assignment.getTeacher() != null) {
            dto.setTeacherName(assignment.getTeacher().getFirstName() + " " +
                    assignment.getTeacher().getLastName());
        }

        if (assignment.getSchool() != null) {
            dto.setSchoolName(assignment.getSchool().getName());
        }

        dto.setStatus(String.valueOf(assignment.getStatus()));

        return dto;
    }
}
