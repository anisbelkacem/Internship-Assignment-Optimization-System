package com.aspd.backend.service;

import com.aspd.backend.dto.TeacherPlConfigDto;
import com.aspd.backend.dto.TeacherPlConfigRequest;
import com.aspd.backend.model.Teacher;
import com.aspd.backend.model.TeacherPlConfig;
import com.aspd.backend.repository.TeacherPlConfigRepository;
import com.aspd.backend.repository.TeacherRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class TeacherPlConfigService {

    private final TeacherRepository teacherRepository;
    private final TeacherPlConfigRepository plConfigRepository;

    public TeacherPlConfigService(TeacherRepository teacherRepository,
                                  TeacherPlConfigRepository plConfigRepository) {
        this.teacherRepository = teacherRepository;
        this.plConfigRepository = plConfigRepository;
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
        cfg.setSubjectSpecializations(request.subjectSpecializations());
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
