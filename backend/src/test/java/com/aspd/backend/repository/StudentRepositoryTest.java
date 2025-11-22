package com.aspd.backend.repository;

import com.aspd.backend.model.Student;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class StudentRepositoryTest {
    @Test
    void printDatasource(@Autowired Environment env) {
        System.out.println("URL = " + env.getProperty("spring.datasource.url"));
    }
    @Autowired
    private StudentRepository studentRepository;

    @Test
    void testSaveAndFind() {
        Student student = new Student();
        student.setMatriculationNbr(50);
        student.setFirstName("Maria");

        studentRepository.save(student);

        Optional<Student> found = studentRepository.findById(50);

        assertTrue(found.isPresent());
        assertEquals("Maria", found.get().getFirstName());
    }
}
