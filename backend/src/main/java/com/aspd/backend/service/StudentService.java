package com.aspd.backend.service;

import com.aspd.backend.common.exception.NotFoundException;
import com.aspd.backend.dto.StudentDto;
import com.aspd.backend.model.Student;
import com.aspd.backend.repository.CourseRepository;
import com.aspd.backend.repository.StudentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class StudentService {

    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;
    private final GeoapifyService geoapifyService;

    public StudentService(StudentRepository studentRepository, CourseRepository courseRepository, GeoapifyService geoapifyService) {
        this.studentRepository = studentRepository;
        this.courseRepository = courseRepository;
        this.geoapifyService = geoapifyService;
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

        // Geocode student addresses
        geocodeAddresses(student);

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

            // Geocode student addresses
            geocodeAddresses(student);

            return studentRepository.save(student);
        }).orElseThrow(() -> new NotFoundException("Student", dto.getMatriculationNbr()));
    }

    public void deleteStudent(int id) {
        studentRepository.deleteById(id);
    }

    /**
     * Geocode student's home and semester addresses using Geoapify API
     */
    private void geocodeAddresses(Student student) {
        if (student.getAddress() != null) {
            var coords = geoapifyService.getCoordinates(student.getAddress());
            if (coords.isPresent()) {
                student.getAddress().setLongitude(coords.get().getLongitude());
                student.getAddress().setLatitude(coords.get().getLatitude());
                log.info("Geocoded student {} home address", student.getMatriculationNbr());
            } else {
                log.warn("Failed to geocode student {} home address", student.getMatriculationNbr());
            }
        }

        if (student.getAddressSemester() != null) {
            var coords = geoapifyService.getCoordinates(student.getAddressSemester());
            if (coords.isPresent()) {
                student.getAddressSemester().setLongitude(coords.get().getLongitude());
                student.getAddressSemester().setLatitude(coords.get().getLatitude());
                log.info("Geocoded student {} semester address", student.getMatriculationNbr());
            } else {
                log.warn("Failed to geocode student {} semester address", student.getMatriculationNbr());
            }
        }
    }
}
