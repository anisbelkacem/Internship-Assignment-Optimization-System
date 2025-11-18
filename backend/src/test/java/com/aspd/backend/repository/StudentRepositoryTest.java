package com.aspd.backend.repository;

import com.aspd.backend.model.Student;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class StudentRepositoryTest {

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
