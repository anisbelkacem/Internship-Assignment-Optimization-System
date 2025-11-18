package com.aspd.backend.service;

import com.aspd.backend.model.CompletedInternships;
import com.aspd.backend.model.Student;
import com.aspd.backend.dto.CompletedInternshipsDto;
import com.aspd.backend.repository.CompletedInternshipsRepository;
import com.aspd.backend.repository.StudentRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CompletedInternshipsService {

    private final CompletedInternshipsRepository completedInternshipsRepository;
    private final StudentRepository studentRepository;

    public CompletedInternshipsService(CompletedInternshipsRepository completedInternshipsRepository,
                                       StudentRepository studentRepository) {
        this.completedInternshipsRepository = completedInternshipsRepository;
        this.studentRepository = studentRepository;
    }

    public CompletedInternships createCompletedInternship(CompletedInternshipsDto dto) {

        Student student = studentRepository.findById(dto.getStudentId())
                .orElseThrow(() -> new RuntimeException("Student not found"));

        CompletedInternships internship = new CompletedInternships(
                student,
                dto.getTeacherId(),
                dto.getSchoolId(),
                dto.getCourse(),
                dto.getStartDate(),
                dto.getEndDate()
        );

        internship.setDescription(dto.getDescription());

        return completedInternshipsRepository.save(internship);
    }

    public Optional<CompletedInternships> getCompletedInternshipById(Long id) {
        return completedInternshipsRepository.findById(id);
    }

    public List<CompletedInternships> getAllCompletedInternships() {
        return completedInternshipsRepository.findAll();
    }

    public List<CompletedInternships> getInternshipsByStudentId(int studentId) {
        return completedInternshipsRepository.findByStudent_MatriculationNbr(studentId);
    }

    public List<CompletedInternships> getInternshipsByTeacherId(Long teacherId) {
        return completedInternshipsRepository.findByTeacherId(teacherId);
    }

    public List<CompletedInternships> getInternshipsBySchoolId(Long schoolId) {
        return completedInternshipsRepository.findBySchoolId(schoolId);
    }

    public CompletedInternships updateCompletedInternship(Long id, CompletedInternshipsDto dto) {

        var internship = completedInternshipsRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Internship not found"));

        if (dto.getStudentId() != 0) {
            Student student = studentRepository.findById(dto.getStudentId())
                    .orElseThrow(() -> new RuntimeException("Student not found"));
            internship.setStudent(student);
        }

        if (dto.getTeacherId() != null) {
            internship.setTeacherId(dto.getTeacherId());
        }

        if (dto.getSchoolId() != null) {
            internship.setSchoolId(dto.getSchoolId());
        }

        if (dto.getCourse() != null) {
            internship.setCourse(dto.getCourse());
        }

        if (dto.getStartDate() != null) {
            internship.setStartDate(dto.getStartDate());
        }

        if (dto.getEndDate() != null) {
            internship.setEndDate(dto.getEndDate());
        }

        if (dto.getDescription() != null) {
            internship.setDescription(dto.getDescription());
        }

        return completedInternshipsRepository.save(internship);
    }

    public void deleteCompletedInternship(Long id) {
        completedInternshipsRepository.deleteById(id);
    }
}
