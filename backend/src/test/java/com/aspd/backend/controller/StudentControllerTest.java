package com.aspd.backend.controller;

import com.aspd.backend.dto.StudentDto;
import com.aspd.backend.model.Student;
import com.aspd.backend.service.StudentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(StudentController.class)
class StudentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StudentService studentService;

    @Test
    void testGetStudentById() throws Exception {
        Student student = new Student();
        student.setMatriculationNbr(1);
        student.setFirstName("John");

        when(studentService.getStudentById(1)).thenReturn(Optional.of(student));

        mockMvc.perform(get("/api/students/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.matriculationNbr").value(1))
                .andExpect(jsonPath("$.firstName").value("John"));
    }
}
