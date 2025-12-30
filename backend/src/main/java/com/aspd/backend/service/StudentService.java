package com.aspd.backend.service;

import com.aspd.backend.common.exception.NotFoundException;
import com.aspd.backend.dto.StudentDto;
import com.aspd.backend.model.Student;
import com.aspd.backend.repository.CourseRepository;
import com.aspd.backend.repository.StudentRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class StudentService {

    private final StudentRepository studentRepository;

    private final CourseRepository courseRepository;

    public StudentService(StudentRepository studentRepository, CourseRepository courseRepository) {
        this.studentRepository = studentRepository;
        this.courseRepository = courseRepository;
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
        student.setMainCourse(courseRepository.findById(dto.getMainCourseId()).orElseThrow());
        student.setPrefCourse1(courseRepository.findById(dto.getPrefCourse1Id()).orElseThrow());
        student.setPrefCourse2(courseRepository.findById(dto.getPrefCourse2Id()).orElseThrow());
        student.setPrefCourse3(courseRepository.findById(dto.getPrefCourse3Id()).orElseThrow());
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
            student.setMainCourse(courseRepository.findById(dto.getMainCourseId())
                    .orElseThrow(() -> new NotFoundException("Course", dto.getMainCourseId())));
            student.setPrefCourse1(courseRepository.findById(dto.getPrefCourse1Id())
                    .orElseThrow(() -> new NotFoundException("Course", dto.getPrefCourse1Id())));
            student.setPrefCourse2(courseRepository.findById(dto.getPrefCourse2Id())
                    .orElseThrow(() -> new NotFoundException("Course", dto.getPrefCourse2Id())));
            student.setPrefCourse3(courseRepository.findById(dto.getPrefCourse3Id())
                    .orElseThrow(() -> new NotFoundException("Course", dto.getPrefCourse3Id())));
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
