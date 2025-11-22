package com.aspd.backend.service;

import com.aspd.backend.common.exception.NotFoundException;
import com.aspd.backend.model.CompletedInternships;
import com.aspd.backend.model.School;
import com.aspd.backend.model.Student;
import com.aspd.backend.model.Teacher;
import com.aspd.backend.dto.CompletedInternshipsDto;
import com.aspd.backend.repository.CompletedInternshipsRepository;
import com.aspd.backend.repository.SchoolRepository;
import com.aspd.backend.repository.StudentRepository;
import com.aspd.backend.repository.TeacherRepository;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CompletedInternshipsService {

    private final CompletedInternshipsRepository completedInternshipsRepository;
    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;
    private final SchoolRepository schoolRepository;

    public CompletedInternshipsService(
            CompletedInternshipsRepository completedInternshipsRepository,
            StudentRepository studentRepository,
            TeacherRepository teacherRepository,
            SchoolRepository schoolRepository
    ) {
        this.completedInternshipsRepository = completedInternshipsRepository;
        this.studentRepository = studentRepository;
        this.teacherRepository = teacherRepository;
        this.schoolRepository = schoolRepository;
    }

    public CompletedInternships createCompletedInternship(CompletedInternshipsDto dto) {
        Student student = studentRepository.findById(dto.getStudentId())
                .orElseThrow(() -> new NotFoundException("Student", dto.getStudentId()));

        Teacher teacher = teacherRepository.findById(dto.getTeacherId())
                .orElseThrow(() -> new NotFoundException("Teacher", dto.getTeacherId()));

        School school = schoolRepository.findById(dto.getSchoolId())
                .orElseThrow(() -> new NotFoundException("School", dto.getSchoolId()));

        CompletedInternships internship = new CompletedInternships(
                student,
                teacher,
                school,
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
        return completedInternshipsRepository.findByTeacher_TeacherId(teacherId);
    }

    public List<CompletedInternships> getInternshipsBySchoolId(Long schoolId) {
        return completedInternshipsRepository.findBySchool_Id(schoolId);
    }

    public CompletedInternships updateCompletedInternship(Long id, CompletedInternshipsDto dto) {

        CompletedInternships internship = completedInternshipsRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("CompletedInternship", id));

        if (dto.getStudentId() != 0) {
            Student student = studentRepository.findById(dto.getStudentId())
                    .orElseThrow(() -> new NotFoundException("Student", dto.getStudentId()));
            internship.setStudent(student);
        }

        if (dto.getTeacherId() != null) {
            Teacher teacher = teacherRepository.findById(dto.getTeacherId())
                    .orElseThrow(() -> new NotFoundException("Teacher", dto.getTeacherId()));
            internship.setTeacher(teacher);
        }

        if (dto.getSchoolId() != null) {
            School school = schoolRepository.findById(dto.getSchoolId())
                    .orElseThrow(() -> new NotFoundException("School", dto.getSchoolId()));
            internship.setSchool(school);
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
