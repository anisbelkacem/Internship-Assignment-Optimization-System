package com.aspd.backend.service;

import com.aspd.backend.model.PlannedInternship;
import com.aspd.backend.model.School;
import com.aspd.backend.model.Teacher;
import com.aspd.backend.repository.PlannedInternshipRepository;
import com.aspd.backend.repository.SchoolRepository;
import com.aspd.backend.repository.TeacherRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service for managing saved PlannedInternship assignments.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class PlannedInternshipService {

    private final PlannedInternshipRepository plannedInternshipRepository;
    private final TeacherRepository teacherRepository;
    private final SchoolRepository schoolRepository;
    private final AuditLogService auditLogService;

    /**
     * Get all planned internships for a specific school year.
     */
    public List<PlannedInternship> getBySchoolYear(String schoolYear) {
        log.info("Fetching planned internships for year: {}", schoolYear);
        return plannedInternshipRepository.findBySchoolYear(schoolYear);
    }

    /**
     * Get a specific planned internship by ID.
     */
    public Optional<PlannedInternship> getById(Long id) {
        return plannedInternshipRepository.findById(id);
    }

    /**
     * Update a planned internship (e.g., change assigned teacher or school).
     */
    @Transactional
    public PlannedInternship update(Long id, Long teacherId, Long schoolId) {
        log.info("Updating planned internship {} with teacher {} and school {}", id, teacherId, schoolId);
        
        PlannedInternship internship = plannedInternshipRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("PlannedInternship not found: " + id));

        // Capture previous state
        Map<String, Object> previousValues = capturePlannedInternshipState(internship);

        // Update teacher if provided
        if (teacherId != null) {
            Teacher teacher = teacherRepository.findById(teacherId)
                    .orElseThrow(() -> new RuntimeException("Teacher not found: " + teacherId));
            internship.setAssignedTeacher(teacher);
        } else {
            internship.setAssignedTeacher(null);
        }

        // Update school if provided
        if (schoolId != null) {
            School school = schoolRepository.findById(schoolId)
                    .orElseThrow(() -> new RuntimeException("School not found: " + schoolId));
            internship.setAssignedSchool(school);
        } else {
            internship.setAssignedSchool(null);
        }

        PlannedInternship saved = plannedInternshipRepository.save(internship);
        
        // Log the change
        Map<String, Object> newValues = capturePlannedInternshipState(saved);
        auditLogService.log(
            "PlannedInternship",
            id,
            "UPDATE",
            "Planned internship updated",
            previousValues,
            newValues
        );
        
        return saved;
    }

    /**
     * Delete a planned internship by ID.
     */
    @Transactional
    public void delete(Long id) {
        log.info("Deleting planned internship: {}", id);
        
        PlannedInternship internship = plannedInternshipRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("PlannedInternship not found: " + id));
        
        Map<String, Object> deletedValues = capturePlannedInternshipState(internship);
        plannedInternshipRepository.deleteById(id);
        
        // Log the deletion
        auditLogService.log(
            "PlannedInternship",
            id,
            "DELETE",
            "Planned internship deleted",
            deletedValues,
            null
        );
    }

    /**
     * Delete all planned internships for a specific school year.
     */
    @Transactional
    public void deleteBySchoolYear(String schoolYear) {
        log.info("Deleting all planned internships for year: {}", schoolYear);
        List<PlannedInternship> internships = plannedInternshipRepository.findBySchoolYear(schoolYear);
        plannedInternshipRepository.deleteAll(internships);
    }

    /**
     * Capture the current state of a planned internship for audit logging
     */
    private Map<String, Object> capturePlannedInternshipState(PlannedInternship internship) {
        Map<String, Object> state = new HashMap<>();
        state.put("id", internship.getId());
        state.put("praktikumType", internship.getPraktikumType().name());
        state.put("schoolType", internship.getSchoolType().name());
        state.put("maxCapacity", internship.getMaxCapacity());
        state.put("currentAssignments", internship.getCurrentAssignments());
        if (internship.getAssignedTeacher() != null) {
            state.put("teacherId", internship.getAssignedTeacher().getTeacherId());
            state.put("teacherName", internship.getAssignedTeacher().getFirstName() + " " + internship.getAssignedTeacher().getLastName());
        }
        if (internship.getAssignedSchool() != null) {
            state.put("schoolId", internship.getAssignedSchool().getId());
            state.put("schoolName", internship.getAssignedSchool().getName());
        }
        if (internship.getCourse() != null) {
            state.put("courseId", internship.getCourse().getId());
            state.put("courseName", internship.getCourse().getName());
        }
        state.put("schoolYear", internship.getSchoolYear());
        return state;
    }

}

