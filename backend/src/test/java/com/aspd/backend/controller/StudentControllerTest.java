package com.aspd.backend.controller;

import com.aspd.backend.model.Student;
import com.aspd.backend.security.JwtAuthenticationFilter;
import com.aspd.backend.security.JwtUtil;
import com.aspd.backend.service.StudentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(StudentController.class)
@AutoConfigureMockMvc(addFilters = false)
class StudentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StudentService studentService;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void testGetStudentById() throws Exception {

        Student student = new Student();
        student.setMatriculationNbr(1);
        student.setFirstName("John");
        student.setLastName("Doe");
        student.setEmail("john@example.com");
        student.setPhone("123456");
        student.setBirthDate(LocalDate.of(2000, 1, 1));

        when(studentService.getStudentById(1)).thenReturn(Optional.of(student));

        mockMvc.perform(get("/api/students/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.matriculationNbr").value(1))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.email").value("john@example.com"));
    }
}
