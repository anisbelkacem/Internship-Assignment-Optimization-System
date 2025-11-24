package com.aspd.backend.service;


import com.aspd.backend.dto.TeacherDto;
import com.aspd.backend.dto.TeacherPlConfigDto;
import com.aspd.backend.dto.TeacherRequest;
import com.aspd.backend.model.Course;
import com.aspd.backend.model.PraktikumType;
import com.aspd.backend.model.Teacher;
import com.aspd.backend.repository.TeacherPlConfigRepository;
import com.aspd.backend.repository.TeacherRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@Transactional
public class TeacherService {

    private final TeacherRepository teacherRepository;
    private final TeacherPlConfigRepository plConfigRepository;

    public TeacherService(TeacherRepository teacherRepository,
                          TeacherPlConfigRepository plConfigRepository) {
        this.teacherRepository = teacherRepository;
        this.plConfigRepository = plConfigRepository;
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
        if (request.mainSubject() == null ) {
            throw new IllegalArgumentException("Main subject is required");
        }
        if (request.email() == null || request.email().isBlank()) {
            throw new IllegalArgumentException("Email is required");
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
        teacher.setMainSubject(request.mainSubject());
        teacher.setSchool(request.schoolId());
        teacher.setEmail(request.email().trim());

    }

    private TeacherDto toDto(Teacher teacher) {
        var configDtos = teacher.getPlConfigs().stream()
                .map(cfg -> new TeacherPlConfigDto(
                        cfg.getId(),
                        cfg.getSchoolYear(),
                        cfg.getMaxPraktikaPerYear(),
                        cfg.getTotalHoursCredit(),
                        cfg.getAvailabilityStatus(),
                        cfg.getSubjectSpecializations(),
                        cfg.getInternshipPreferences()
                ))
                .toList();

        return new TeacherDto(
                teacher.getTeacherId(),
                teacher.getFirstName(),
                teacher.getLastName(),
                teacher.getMainSubject(),
                teacher.getSchool(),
                teacher.getEmail(),
                configDtos
        );
    }

}
