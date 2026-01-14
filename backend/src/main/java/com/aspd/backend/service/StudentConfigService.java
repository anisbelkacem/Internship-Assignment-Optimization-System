package com.aspd.backend.service;

import com.aspd.backend.common.exception.NotFoundException;
import com.aspd.backend.dto.StudentConfigDto;
import com.aspd.backend.model.Student;
import com.aspd.backend.model.StudentConfig;
import com.aspd.backend.repository.StudentConfigRepository;
import com.aspd.backend.repository.StudentRepository;
import com.aspd.backend.validation.AssignmentValidationException;
import com.aspd.backend.validation.StudentConfigValidationService;
import com.aspd.backend.validation.ValidationResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StudentConfigService {

    private final StudentConfigRepository configRepository;
    private final StudentRepository studentRepository;
    private final AuditLogService auditLogService;
    private final StudentConfigValidationService validationService;

    public StudentConfigDto createConfig(StudentConfigDto dto) {
        ValidationResult vr = validationService.validate(dto);
        if (!vr.isHardValid()) throw new AssignmentValidationException(vr);

        Student student = studentRepository.findById(dto.getStudentId())
                .orElseThrow(() -> new NotFoundException("Student", dto.getStudentId()));

        StudentConfig config = new StudentConfig(
                student,
                dto.getYear(),
                dto.isPdpI(),
                dto.isPdpII(),
                dto.isZsp(),
                dto.isSfp()
        );

        StudentConfig saved = configRepository.save(config);
        
        // Log the creation
        Map<String, Object> newValues = captureConfigState(saved);
        auditLogService.log(
            "StudentConfig",
            saved.getId(),
            "CREATE",
            "Student config created for student " + student.getMatriculationNbr(),
            null,
            newValues
        );
        
        return toDto(saved);
    }


    public StudentConfigDto getConfig(int studentId, String year) {
        StudentConfig config = configRepository
                .findByStudent_MatriculationNbrAndYear(studentId, year)
                .orElseThrow(() -> new NotFoundException("StudentConfig", studentId, "Year: " + year));

        return toDto(config);
    }
    public List<StudentConfigDto> getAllConfigs() {
        return configRepository.findAll()
                .stream()
                .map(this::toDto)
                .toList();
    }
    public List<StudentConfigDto> getConfigsByStudent(int studentId) {
        return configRepository.findByStudent_MatriculationNbr(studentId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    public List<StudentConfigDto> getConfigsByYear(String year) {
        return configRepository.findByYear(year)
                .stream()
                .map(this::toDto)
                .toList();
    }

    public StudentConfigDto updateConfig(Long id, StudentConfigDto dto) {
        dto.setId(id);

        ValidationResult vr = validationService.validate(dto);
        if (!vr.isHardValid()) throw new AssignmentValidationException(vr);

        StudentConfig config = configRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("StudentConfig", id));

        // Capture previous state
        Map<String, Object> previousValues = captureConfigState(config);

        config.setYear(dto.getYear());
        config.setMainCourse(dto.getMainCourse());
        config.setPrefCourse1(dto.getPrefCourse1());
        config.setPrefCourse2(dto.getPrefCourse2());
        config.setPrefCourse3(dto.getPrefCourse3());
        config.setPdpI(dto.isPdpI());
        config.setPdpII(dto.isPdpII());
        config.setZsp(dto.isZsp());
        config.setSfp(dto.isSfp());

        StudentConfig saved = configRepository.save(config);
        
        // Log the update
        Map<String, Object> newValues = captureConfigState(saved);
        auditLogService.log(
            "StudentConfig",
            id,
            "UPDATE",
            "Student config updated",
            previousValues,
            newValues
        );

        return toDto(saved);
    }

    public void deleteConfig(Long id) {
        StudentConfig config = configRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("StudentConfig", id));
        
        Map<String, Object> deletedValues = captureConfigState(config);
        configRepository.deleteById(id);
        
        // Log the deletion
        auditLogService.log(
            "StudentConfig",
            id,
            "DELETE",
            "Student config deleted",
            deletedValues,
            null
        );
    }
    public List<String> getAllYears() {
        return configRepository.findDistinctYears();
    }

    private StudentConfigDto toDto(StudentConfig config) {
        return new StudentConfigDto(
                config.getId(),
                config.getStudent().getMatriculationNbr(),
                config.getYear(),
                config.getMainCourse(),
                config.getPrefCourse1(),
                config.getPrefCourse2(),
                config.getPrefCourse3(),
                config.getSchoolType(),
                config.isPdpI(),
                config.isPdpII(),
                config.isZsp(),
                config.isSfp()
        );
    }

    private Map<String, Object> captureConfigState(StudentConfig config) {
        Map<String, Object> state = new HashMap<>();
        state.put("id", config.getId());
        state.put("studentId", config.getStudent().getMatriculationNbr());
        state.put("year", config.getYear());
        state.put("mainCourse", config.getMainCourse());
        state.put("prefCourse1", config.getPrefCourse1());
        state.put("prefCourse2", config.getPrefCourse2());
        state.put("prefCourse3", config.getPrefCourse3());
        state.put("pdpI", config.isPdpI());
        state.put("pdpII", config.isPdpII());
        state.put("zsp", config.isZsp());
        state.put("sfp", config.isSfp());
        return state;
    }
}
