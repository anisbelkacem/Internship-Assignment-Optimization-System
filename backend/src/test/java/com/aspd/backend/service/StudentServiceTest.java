package com.aspd.backend.service;

import com.aspd.backend.dto.StudentDto;
import com.aspd.backend.model.Address;
import com.aspd.backend.model.Course;
import com.aspd.backend.model.Student;
import com.aspd.backend.repository.CourseRepository;
import com.aspd.backend.repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class StudentServiceTest {

    private StudentRepository studentRepository;
    private StudentService studentService;
    private CourseRepository courseRepository;
    private GeoapifyService geoapifyService;


    @BeforeEach
    void setup() {
        studentRepository = mock(StudentRepository.class);
        courseRepository = mock(CourseRepository.class);
        geoapifyService = mock(GeoapifyService.class);
        studentService = new StudentService(studentRepository, courseRepository, geoapifyService);
    }

    @Test
    void testGetStudentById() {
        Student student = new Student();
        student.setMatriculationNbr(1);

        when(studentRepository.findById(1)).thenReturn(Optional.of(student));

        Optional<Student> result = studentService.getStudentById(1);

        assertTrue(result.isPresent());
        assertEquals(1, result.get().getMatriculationNbr());
        verify(studentRepository).findById(1);
    }

    @Test
    void testCreateStudent() {
        StudentDto dto = new StudentDto();
        dto.setMatriculationNbr(10);
        dto.setFirstName("John");
        dto.setAddress(new Address());
        dto.setAddressSemester(new Address());

        dto.setMainCourseId(1L);
        dto.setPrefCourse1Id(2L);
        dto.setPrefCourse2Id(3L);
        dto.setPrefCourse3Id(4L);

        when(courseRepository.findById(1L)).thenReturn(Optional.of(new Course("MAIN")));
        when(courseRepository.findById(2L)).thenReturn(Optional.of(new Course("PREF1")));
        when(courseRepository.findById(3L)).thenReturn(Optional.of(new Course("PREF2")));
        when(courseRepository.findById(4L)).thenReturn(Optional.of(new Course("PREF3")));

        ArgumentCaptor<Student> captor = ArgumentCaptor.forClass(Student.class);

        when(studentRepository.save(any(Student.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Student saved = studentService.createStudent(dto);

        verify(studentRepository).save(captor.capture());

        assertEquals("John", captor.getValue().getFirstName());
        assertEquals(10, saved.getMatriculationNbr());
    }

    @Test
    void testUpdateStudent() {
        Student student = new Student();
        student.setMatriculationNbr(5);
        student.setFirstName("Old");
        student.setAddress(new Address());
        student.setAddressSemester(new Address());

        StudentDto dto = new StudentDto();
        dto.setFirstName("New");
        dto.setAddress(new Address());
        dto.setAddressSemester(new Address());

        // Set course IDs
        dto.setMainCourseId(1L);
        dto.setPrefCourse1Id(2L);
        dto.setPrefCourse2Id(3L);
        dto.setPrefCourse3Id(4L);

        // Mock course repository
        when(courseRepository.findById(1L)).thenReturn(Optional.of(new Course("MAIN")));
        when(courseRepository.findById(2L)).thenReturn(Optional.of(new Course("PREF1")));
        when(courseRepository.findById(3L)).thenReturn(Optional.of(new Course("PREF2")));
        when(courseRepository.findById(4L)).thenReturn(Optional.of(new Course("PREF3")));


        when(studentRepository.findById(5)).thenReturn(Optional.of(student));
        when(studentRepository.save(any(Student.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Student updated = studentService.updateStudent(5, dto);

        assertEquals("New", updated.getFirstName());
        verify(studentRepository).save(student);
    }
}
