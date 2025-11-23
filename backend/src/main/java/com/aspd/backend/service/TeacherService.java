package com.aspd.backend.service;


import com.aspd.backend.dto.TeacherDto;
import com.aspd.backend.dto.TeacherRequest;
import com.aspd.backend.model.PraktikumType;
import com.aspd.backend.model.Teacher;
import com.aspd.backend.repository.TeacherRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@Transactional
public class TeacherService {

    private final TeacherRepository teacherRepository;

    public TeacherService(TeacherRepository teacherRepository) {
        this.teacherRepository = teacherRepository;
    }

    // List all PLs (teachers)
    public List<TeacherDto> getAll() {
        return teacherRepository.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    // Get one by id
    public TeacherDto getById(Long id) {
        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Teacher (PL) not found with id " + id));
        return toDto(teacher);
    }

    // Create
    public TeacherDto create(TeacherRequest request) {
        validateRequest(request, null);

        Teacher teacher = new Teacher();
        applyRequest(teacher, request);

        teacher = teacherRepository.save(teacher);
        return toDto(teacher);
    }

    // Update
    public TeacherDto update(Long id, TeacherRequest request) {
        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Teacher (PL) not found with id " + id));

        validateRequest(request, id);
        applyRequest(teacher, request);

        teacher = teacherRepository.save(teacher);
        return toDto(teacher);
    }

    // Delete
    public void delete(Long id) {
        if (!teacherRepository.existsById(id)) {
            throw new IllegalArgumentException("Teacher (PL) not found with id " + id);
        }
        teacherRepository.deleteById(id);
    }

    // ----- Helpers -----

    private void validateRequest(TeacherRequest request, Long currentId) {
        if (request.firstName() == null || request.firstName().isBlank()) {
            throw new IllegalArgumentException("First name is required");
        }
        if (request.lastName() == null || request.lastName().isBlank()) {
            throw new IllegalArgumentException("Last name is required");
        }
        if (request.mainSubject() == null || request.mainSubject().isBlank()) {
            throw new IllegalArgumentException("Main subject is required");
        }
        if (request.email() == null || request.email().isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }

        if (request.maxPraktikaPerYear() != null && request.maxPraktikaPerYear() < 0) {
            throw new IllegalArgumentException("maxPraktikaPerYear must be >= 0");
        }

        if (request.totalHoursCredit() != null && request.totalHoursCredit() < 0) {
            throw new IllegalArgumentException("totalHoursCredit must be >= 0");
        }

        Set<String> subjects = request.subjectSpecializations();
        if (subjects == null || subjects.isEmpty()) {
            throw new IllegalArgumentException("At least one subject specialization is required");
        }

        Set<PraktikumType> prefs = request.internshipPreferences();
        if (prefs == null || prefs.isEmpty()) {
            throw new IllegalArgumentException("At least one internship preference is required");
        }

        // Uniqueness by email
        teacherRepository.findByEmail(request.email()).ifPresent(existing -> {
            if (currentId == null || !existing.getTeacherId().equals(currentId)) {
                throw new IllegalArgumentException("A teacher/PL with this email already exists");
            }
        });
    }

    private void applyRequest(Teacher teacher, TeacherRequest request) {
        teacher.setFirstName(request.firstName().trim());
        teacher.setLastName(request.lastName().trim());
        teacher.setMainSubject(request.mainSubject().trim());
        teacher.setSchoolId(request.schoolId());
        teacher.setMaxPraktikaPerYear(request.maxPraktikaPerYear());
        teacher.setEmail(request.email().trim());
        teacher.setTotalHoursCredit(request.totalHoursCredit());
        teacher.setAvailabilityStatus(request.availabilityStatus());
        teacher.setSubjectSpecializations(request.subjectSpecializations());
        teacher.setInternshipPreferences(request.internshipPreferences());
    }

    private TeacherDto toDto(Teacher teacher) {
        return new TeacherDto(
                teacher.getTeacherId(),
                teacher.getFirstName(),
                teacher.getLastName(),
                teacher.getMainSubject(),
                teacher.getSchoolId(),
                teacher.getMaxPraktikaPerYear(),
                teacher.getEmail(),
                teacher.getTotalHoursCredit(),
                teacher.getAvailabilityStatus(),
                teacher.getSubjectSpecializations(),
                teacher.getInternshipPreferences()
        );
    }
}
