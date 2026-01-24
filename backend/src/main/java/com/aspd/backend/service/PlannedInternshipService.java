package com.aspd.backend.service;

import com.aspd.backend.model.PlannedInternship;
import com.aspd.backend.model.School;
import com.aspd.backend.model.Teacher;
import com.aspd.backend.repository.PlannedInternshipRepository;
import com.aspd.backend.repository.SchoolRepository;
import com.aspd.backend.repository.StudentInternshipDemandRepository;
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
    private final InternshipAssignmentService internshipAssignmentService;
    private final StudentInternshipDemandRepository studentInternshipDemandRepository;
    private final PlannedInternshipValidationService plannedInternshipValidationService;


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
        // Server-side guard: never allow persisting an update that violates HARD constraints
        ValidationResult vr = plannedInternshipValidationService
                .validatePlannedInternshipUpdate(id, teacherId, schoolId);

        if (!vr.isHardValid()) {
            throw new AssignmentValidationException(vr);
        }


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
     * Deletes in cascade order to avoid foreign key constraint violations:
     * - student_internship_demands (references planned_internships)
     * - internship_assignments (references planned_internships)
     * - planned_internships
     */
    @Transactional
    public void deleteBySchoolYear(String schoolYear) {
        log.info("Deleting all planned internships and related data for year: {}", schoolYear);

        //Delete all student internship demands (they reference planned internships)
        try {
            studentInternshipDemandRepository.deleteBySchoolYear(schoolYear);
            log.info("Deleted all student internship demands for year: {}", schoolYear);
        } catch (Exception e) {
            log.warn("Error deleting student internship demands for year {}: {}", schoolYear, e.getMessage());
        }

        //Delete all internship assignments (they reference planned internships)
        int deletedAssignments = internshipAssignmentService.deleteBySchoolYear(schoolYear);
        log.info("Deleted {} internship assignments for year: {}", deletedAssignments, schoolYear);

        //Now safe to delete planned internships
        List<PlannedInternship> internships = plannedInternshipRepository.findBySchoolYear(schoolYear);
        plannedInternshipRepository.deleteAll(internships);
        log.info("Deleted {} planned internships for year: {}", internships.size(), schoolYear);
    }
}
