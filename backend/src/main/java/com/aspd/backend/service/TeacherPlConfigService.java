package com.aspd.backend.service;

import com.aspd.backend.dto.TeacherPlConfigDto;
import com.aspd.backend.dto.TeacherPlConfigRequest;
import com.aspd.backend.model.Course;
import com.aspd.backend.model.Teacher;
import com.aspd.backend.model.TeacherPlConfig;
import com.aspd.backend.repository.CourseRepository;
import com.aspd.backend.repository.TeacherPlConfigRepository;
import com.aspd.backend.repository.TeacherRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Transactional
public class TeacherPlConfigService {

    private final TeacherRepository teacherRepository;
    private final TeacherPlConfigRepository plConfigRepository;
    private final CourseRepository courseRepository; // Add this


    public TeacherPlConfigService(TeacherRepository teacherRepository,
                                  TeacherPlConfigRepository plConfigRepository,
                                  CourseRepository courseRepository) {
        this.teacherRepository = teacherRepository;
        this.plConfigRepository = plConfigRepository;
        this.courseRepository = courseRepository;

    }

    public List<TeacherPlConfigDto> getForTeacher(Long teacherId) {
        return plConfigRepository.findByTeacher_TeacherId(teacherId).stream()
                .map(this::toDto)
                .toList();
    }

    public TeacherPlConfigDto create(Long teacherId, TeacherPlConfigRequest request) {
        Teacher teacher = teacherRepository.findById(teacherId)
                .orElseThrow(() -> new IllegalArgumentException("Teacher not found: " + teacherId));

        TeacherPlConfig cfg = new TeacherPlConfig();
        cfg.setTeacher(teacher);
        applyRequest(cfg, request);

        cfg = plConfigRepository.save(cfg);
        return toDto(cfg);
    }

    public TeacherPlConfigDto update(Long configId, TeacherPlConfigRequest request) {
        TeacherPlConfig cfg = plConfigRepository.findById(configId)
                .orElseThrow(() -> new IllegalArgumentException("PL config not found: " + configId));

        applyRequest(cfg, request);

        cfg = plConfigRepository.save(cfg);
        return toDto(cfg);
    }

    public void delete(Long configId) {
        plConfigRepository.deleteById(configId);
    }

    private void applyRequest(TeacherPlConfig cfg, TeacherPlConfigRequest request) {
        cfg.setSchoolYear(request.schoolYear());
        cfg.setMaxPraktikaPerYear(request.maxPraktikaPerYear());
        cfg.setTotalHoursCredit(request.totalHoursCredit());
        cfg.setAvailabilityStatus(request.availabilityStatus());
        Set<Course> courses = new HashSet<>();
        if (request.subjectSpecializations() != null) {
            for (Long courseId : request.subjectSpecializations()) {
                Course course = courseRepository.findById(courseId)
                        .orElseThrow(() -> new IllegalArgumentException("Course not found: " + courseId));
                courses.add(course);
            }
        }
        cfg.setSubjectSpecializations(courses);
        cfg.setInternshipPreferences(request.internshipPreferences());
    }

    private TeacherPlConfigDto toDto(TeacherPlConfig cfg) {
        return new TeacherPlConfigDto(
                cfg.getId(),
                cfg.getSchoolYear(),
                cfg.getMaxPraktikaPerYear(),
                cfg.getTotalHoursCredit(),
                cfg.getAvailabilityStatus(),
                cfg.getSubjectSpecializations(),
                cfg.getInternshipPreferences()
        );
    }
}
