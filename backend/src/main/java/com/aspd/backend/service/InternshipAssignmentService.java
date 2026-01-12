package com.aspd.backend.service;

import com.aspd.backend.model.AssignmentStatus;
import com.aspd.backend.model.InternshipAssignment;
import com.aspd.backend.repository.InternshipAssignmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service layer for managing internship assignments.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InternshipAssignmentService {

    private final InternshipAssignmentRepository assignmentRepository;

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
                    if (updatedAssignment.getStartDate() != null) {
                        existing.setStartDate(updatedAssignment.getStartDate());
                    }
                    if (updatedAssignment.getEndDate() != null) {
                        existing.setEndDate(updatedAssignment.getEndDate());
                    }
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
                    return assignmentRepository.save(existing);
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
                    assignment.setStatus(status);
                    return assignmentRepository.save(assignment);
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
        
        assignmentRepository.deleteById(id);
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
}
