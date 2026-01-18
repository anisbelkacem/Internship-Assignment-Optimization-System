package com.aspd.backend.service;

import com.aspd.backend.model.PlannedInternship;
import com.aspd.backend.model.Teacher;
import com.aspd.backend.repository.PlannedInternshipRepository;
import com.aspd.backend.repository.TeacherRepository;
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
        log.info("Updating planned internship {} with teacher {} (school derived from teacher; school param ignored)", id, teacherId);
        
        PlannedInternship internship = plannedInternshipRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("PlannedInternship not found: " + id));

        // Update teacher if provided
        if (teacherId != null) {
            Teacher teacher = teacherRepository.findById(teacherId)
                    .orElseThrow(() -> new RuntimeException("Teacher not found: " + teacherId));
            internship.setAssignedTeacher(teacher);
        } else {
            internship.setAssignedTeacher(null);
        }

        // School is derived from the assigned teacher; ignore schoolId

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
