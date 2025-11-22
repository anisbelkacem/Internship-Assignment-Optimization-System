package com.aspd.backend.repository;

import com.aspd.backend.model.Address;
import com.aspd.backend.model.Student;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class StudentRepositoryTest {

    @Autowired
    private StudentRepository studentRepository;

    @Test
    void testSaveAndFind() {
        Student student = new Student();
        student.setMatriculationNbr(50);
        student.setFirstName("Maria");
        student.setLastName("Test");
        student.setEmail("maria@test.com");

        // Addresses must not be null due to @Embedded
        student.setAddress(new Address());
        student.setAddressSemester(new Address());

        studentRepository.save(student);

        Optional<Student> found = studentRepository.findById(50);

        assertTrue(found.isPresent());
        assertEquals("Maria", found.get().getFirstName());
        assertEquals(50, found.get().getMatriculationNbr());
    }
}
