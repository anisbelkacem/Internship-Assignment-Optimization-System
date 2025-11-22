package com.aspd.backend.service;

import com.aspd.backend.common.exception.NotFoundException;
import com.aspd.backend.dto.StudentDto;
import com.aspd.backend.model.Student;
import com.aspd.backend.repository.StudentRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class StudentService {

    private final StudentRepository studentRepository;

    public StudentService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }

    public Optional<Student> getStudentById(int id) {
        return studentRepository.findById(id);
    }

    public Student createStudent(StudentDto dto) {
        Student student = new Student();
        student.setMatriculationNbr(dto.getMatriculationNbr());
        student.setFirstName(dto.getFirstName());
        student.setLastName(dto.getLastName());
        student.setEmail(dto.getEmail());
        student.setSchoolType(dto.getSchoolType());
        student.setMainCourse(dto.getMainCourse());
        student.setPrefCourse1(dto.getPrefCourse1());
        student.setPrefCourse2(dto.getPrefCourse2());
        student.setPrefCourse3(dto.getPrefCourse3());
        student.setRegistred(dto.isRegistred());
        student.setOriented(dto.isOriented());
        student.setAddress(dto.getAddress());
        student.setAddressSemester(dto.getAddressSemester());
        student.setPhone(dto.getPhone());
        student.setBirthDate(dto.getBirthDate());
        student.setDescription(dto.getDescription());

        return studentRepository.save(student);
    }

    public Student updateStudent(int id, StudentDto dto) {
        return studentRepository.findById(id).map(student -> {
            student.setFirstName(dto.getFirstName());
            student.setLastName(dto.getLastName());
            student.setEmail(dto.getEmail());
            student.setSchoolType(dto.getSchoolType());
            student.setMainCourse(dto.getMainCourse());
            student.setPrefCourse1(dto.getPrefCourse1());
            student.setPrefCourse2(dto.getPrefCourse2());
            student.setPrefCourse3(dto.getPrefCourse3());
            student.setRegistred(dto.isRegistred());
            student.setOriented(dto.isOriented());
            student.setAddress(dto.getAddress());
            student.setAddressSemester(dto.getAddressSemester());
            student.setPhone(dto.getPhone());
            student.setBirthDate(dto.getBirthDate());
            student.setDescription(dto.getDescription());
            return studentRepository.save(student);
        }).orElseThrow(() -> new NotFoundException("Student", dto.getMatriculationNbr()));
    }

    public void deleteStudent(int id) {
        studentRepository.deleteById(id);
    }
}
