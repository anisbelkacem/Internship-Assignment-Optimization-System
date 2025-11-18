package com.aspd.backend.service;

import com.aspd.backend.dto.StudentDto;
import com.aspd.backend.model.Student;
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

    @BeforeEach
    void setup() {
        studentRepository = mock(StudentRepository.class);
        studentService = new StudentService(studentRepository);
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

        StudentDto dto = new StudentDto();
        dto.setFirstName("New");

        when(studentRepository.findById(5)).thenReturn(Optional.of(student));
        when(studentRepository.save(any(Student.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Student updated = studentService.updateStudent(5, dto);

        assertEquals("New", updated.getFirstName());
    }
}
