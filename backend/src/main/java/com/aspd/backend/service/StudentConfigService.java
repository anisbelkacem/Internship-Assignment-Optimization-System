package com.aspd.backend.service;

import com.aspd.backend.common.exception.NotFoundException;
import com.aspd.backend.dto.StudentConfigDto;
import com.aspd.backend.model.Student;
import com.aspd.backend.model.StudentConfig;
import com.aspd.backend.repository.StudentConfigRepository;
import com.aspd.backend.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StudentConfigService {

    private final StudentConfigRepository configRepository;
    private final StudentRepository studentRepository;

    public StudentConfigDto createConfig(StudentConfigDto dto) {
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

        configRepository.save(config);
        return toDto(config);
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
        StudentConfig config = configRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("StudentConfig", id));

        config.setYear(dto.getYear());
        config.setMainCourse(dto.getMainCourse());
        config.setPrefCourse1(dto.getPrefCourse1());
        config.setPrefCourse2(dto.getPrefCourse2());
        config.setPrefCourse3(dto.getPrefCourse3());
        config.setPdpI(dto.isPdpI());
        config.setPdpII(dto.isPdpII());
        config.setZsp(dto.isZsp());
        config.setSfp(dto.isSfp());

        configRepository.save(config);

        return toDto(config);
    }

    public void deleteConfig(Long id) {
        if (!configRepository.existsById(id)) {
            throw new NotFoundException("StudentConfig", id);
        }
        configRepository.deleteById(id);
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
}
