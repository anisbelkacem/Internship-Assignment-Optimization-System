package com.aspd.backend.service;

import com.aspd.backend.dto.InternshipAssignmentUpdateRequest;
import com.aspd.backend.model.AssignmentStatus;
import com.aspd.backend.model.InternshipAssignment;
import com.aspd.backend.model.School;
import com.aspd.backend.model.Teacher;
import com.aspd.backend.repository.InternshipAssignmentRepository;
import com.aspd.backend.repository.SchoolRepository;
import com.aspd.backend.repository.TeacherRepository;
import com.aspd.backend.validation.AssignmentValidationException;
import com.aspd.backend.validation.InternshipAssignmentValidationService;
import com.aspd.backend.validation.ValidationResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service layer for managing internship assignments.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InternshipAssignmentService {

    private final InternshipAssignmentRepository assignmentRepository;
    private final AuditLogService auditLogService;
    private final UserService userService;
    private final TeacherRepository teacherRepository;
    private final SchoolRepository schoolRepository;
    private final InternshipAssignmentValidationService validationService;

    /**
     * Get all assignments for a specific school year.
     */
    public List<InternshipAssignment> getBySchoolYear(String schoolYear) {
        log.info("Fetching assignments for school year: {}", schoolYear);
        return assignmentRepository.findBySchoolYear(schoolYear);
    }

    /**
     * Get all assignments.
     */
    public List<InternshipAssignment> getAllAssignments() {
        log.info("Fetching all assignments");
        return assignmentRepository.findAll();
    }

    /**
     * Get a single assignment by ID.
     */
    public Optional<InternshipAssignment> getById(Long id) {
        log.info("Fetching assignment with ID: {}", id);
        return assignmentRepository.findById(id);
    }

    /**
     * Update an assignment.
     */
    @Transactional
    public Optional<InternshipAssignment> update(Long id, InternshipAssignment updatedAssignment) {
        log.info("Updating assignment with ID: {}", id);
        
        return assignmentRepository.findById(id)
                .map(existing -> {
                    // Capture previous values for audit log
                    Map<String, Object> previousValues = captureAssignmentState(existing);
                    
                    if (updatedAssignment.getStatus() != null) {
                        existing.setStatus(updatedAssignment.getStatus());
                    }
                    if (updatedAssignment.getTeacher() != null) {
                        existing.setTeacher(updatedAssignment.getTeacher());
                    }
                    if (updatedAssignment.getSchool() != null) {
                        existing.setSchool(updatedAssignment.getSchool());
                    }
                    if (updatedAssignment.getPraktikumType() != null) {
                        existing.setPraktikumType(updatedAssignment.getPraktikumType());
                    }
                    if (updatedAssignment.getCourse() != null) {
                        existing.setCourse(updatedAssignment.getCourse());
                    }
                    if (updatedAssignment.getStudentConfig() != null) {
                        existing.setStudentConfig(updatedAssignment.getStudentConfig());
                    }
                    if (updatedAssignment.getPlannedInternship() != null) {
                        existing.setPlannedInternship(updatedAssignment.getPlannedInternship());
                    }
                    if (updatedAssignment.getSchoolYear() != null) {
                        existing.setSchoolYear(updatedAssignment.getSchoolYear());
                    }
                    
                    InternshipAssignment saved = assignmentRepository.save(existing);
                    
                    // Log the change
                    Map<String, Object> newValues = captureAssignmentState(saved);
                    auditLogService.log(
                        "InternshipAssignment",
                        id,
                        "UPDATE",
                        "Internship assignment updated",
                        previousValues,
                        newValues
                    );
                    
                    return saved;
                });
    }

    /**
     * Update assignment status.
     */
    @Transactional
    public Optional<InternshipAssignment> updateStatus(Long id, AssignmentStatus status) {
        log.info("Updating assignment {} status to: {}", id, status);
        
        return assignmentRepository.findById(id)
                .map(assignment -> {
                    AssignmentStatus oldStatus = assignment.getStatus();
                    assignment.setStatus(status);
                    InternshipAssignment saved = assignmentRepository.save(assignment);
                    
                    // Log the change
                    auditLogService.log(
                        "InternshipAssignment",
                        id,
                        "UPDATE",
                        "Assignment status changed from " + oldStatus + " to " + status,
                        Map.of("status", oldStatus.name()),
                        Map.of("status", status.name())
                    );
                    
                    return saved;
                });
    }

    /**
     * Delete an assignment by ID.
     */
    @Transactional
    public boolean delete(Long id) {
        log.info("Deleting assignment with ID: {}", id);
        
        if (!assignmentRepository.existsById(id)) {
            return false;
        }
        
        // Capture assignment details before deletion
        InternshipAssignment assignment = assignmentRepository.findById(id).orElse(null);
        if (assignment != null) {
            Map<String, Object> deletedValues = captureAssignmentState(assignment);
            
            assignmentRepository.deleteById(id);
            
            // Log the deletion
            auditLogService.log(
                "InternshipAssignment",
                id,
                "DELETE",
                "Internship assignment deleted",
                deletedValues,
                null
            );
        }
        
        return true;
    }

    /**
     * Delete all assignments for a specific school year.
     */
    @Transactional
    public int deleteBySchoolYear(String schoolYear) {
        log.info("Deleting all assignments for school year: {}", schoolYear);
        
        List<InternshipAssignment> assignments = assignmentRepository.findBySchoolYear(schoolYear);
        
        if (assignments.isEmpty()) {
            return 0;
        }
        
        assignmentRepository.deleteAll(assignments);
        log.info("Deleted {} assignments for school year {}", assignments.size(), schoolYear);
        
        return assignments.size();
    }

    /**
     * Capture the current state of an assignment for audit logging
     */
    private Map<String, Object> captureAssignmentState(InternshipAssignment assignment) {
        Map<String, Object> state = new HashMap<>();
        state.put("id", assignment.getId());
        state.put("status", assignment.getStatus().name());
        state.put("praktikumType", assignment.getPraktikumType().name());
        if (assignment.getTeacher() != null) {
            state.put("teacherId", assignment.getTeacher().getTeacherId());
            state.put("teacherName", assignment.getTeacher().getFirstName() + " " + assignment.getTeacher().getLastName());
        }
        if (assignment.getSchool() != null) {
            state.put("schoolId", assignment.getSchool().getId());
            state.put("schoolName", assignment.getSchool().getName());
        }
        if (assignment.getCourse() != null) {
            state.put("courseId", assignment.getCourse().getId());
            state.put("courseName", assignment.getCourse().getName());
        }
        state.put("schoolYear", assignment.getSchoolYear());
        return state;
    }

    @Transactional
    public Optional<InternshipAssignment> updateByIds(Long id, InternshipAssignmentUpdateRequest req) {

        ValidationResult vr = validationService.validateUpdate(id, req.getTeacherId(), req.getSchoolId());
        if (!vr.isHardValid()) throw new AssignmentValidationException(vr);

        return assignmentRepository.findById(id).map(existing -> {

            if (req.getStatus() != null) existing.setStatus(req.getStatus());

            if (req.getTeacherId() != null) {
                Teacher t = teacherRepository.findById(req.getTeacherId())
                        .orElseThrow(() -> new RuntimeException("Teacher not found: " + req.getTeacherId()));
                existing.setTeacher(t);
            }

            if (req.getSchoolId() != null) {
                School s = schoolRepository.findById(req.getSchoolId())
                        .orElseThrow(() -> new RuntimeException("School not found: " + req.getSchoolId()));
                existing.setSchool(s);
            }

            return assignmentRepository.save(existing);
        });
    }
}

