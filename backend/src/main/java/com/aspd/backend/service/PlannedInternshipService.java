package com.aspd.backend.service;

import com.aspd.backend.model.PlannedInternship;
import com.aspd.backend.model.School;
import com.aspd.backend.model.Teacher;
import com.aspd.backend.repository.PlannedInternshipRepository;
import com.aspd.backend.repository.SchoolRepository;
import com.aspd.backend.repository.TeacherRepository;
import com.aspd.backend.validation.AssignmentValidationException;
import com.aspd.backend.validation.PlannedInternshipValidationService;
import com.aspd.backend.validation.ValidationResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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
    private final PlannedInternshipValidationService validationService;


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

        ValidationResult validation = validationService.validatePlannedInternshipUpdate(id, teacherId, schoolId);
        if (!validation.isHardValid()) {
            throw new AssignmentValidationException(validation);
        }

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

        return plannedInternshipRepository.save(internship);
    }

    /**
     * Delete a planned internship by ID.
     */
    @Transactional
    public void delete(Long id) {
        log.info("Deleting planned internship: {}", id);
        plannedInternshipRepository.deleteById(id);
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
}
