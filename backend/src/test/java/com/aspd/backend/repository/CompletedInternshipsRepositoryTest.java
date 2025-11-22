package com.aspd.backend.repository;

import com.aspd.backend.model.CompletedInternships;
import com.aspd.backend.model.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class CompletedInternshipsRepositoryTest {
    @Autowired
    private CompletedInternshipsRepository completedInternshipsRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Test
    @DisplayName("save and find by teacherId and schoolId")
    @Transactional
    void saveAndFindByTeacherAndSchool() {
        Student s = new Student();
        s.setMatriculationNbr(1001);
        s.setFirstName("Test");
        s.setLastName("Student");
        s.setEmail("test@student.com");
        s.setAddress(new Address());
        s.setAddressSemester(new Address());
        studentRepository.save(s);

        CompletedInternships ci = new CompletedInternships();
        ci.setStudent(s);
        ci.setTeacherId(200L);
        ci.setSchoolId(300L);
        ci.setCourse(Course.COMPUTER_SCIENCE);
        ci.setStartDate(LocalDate.of(2024, 1, 1));
        ci.setEndDate(LocalDate.of(2024, 6, 30));
        ci.setDescription("Test internship");

        completedInternshipsRepository.save(ci);

        List<CompletedInternships> byTeacher = completedInternshipsRepository.findByTeacherId(200L);
        assertFalse(byTeacher.isEmpty());
        assertEquals(200L, byTeacher.get(0).getTeacherId());

        List<CompletedInternships> bySchool = completedInternshipsRepository.findBySchoolId(300L);
        assertFalse(bySchool.isEmpty());
        assertEquals(300L, bySchool.get(0).getSchoolId());
    }


    @Test
    @DisplayName("find by student's matriculation number")
    void findByStudentMatriculationNumber() {
        Student s = new Student();
        s.setMatriculationNbr(5555);
        s.setFirstName("Find");
        s.setLastName("Me");
        studentRepository.saveAndFlush(s);

        CompletedInternships ci = new CompletedInternships();
        ci.setStudent(s);
        ci.setTeacherId(10L);
        ci.setSchoolId(20L);
        ci.setCourse(Course.OTHER);
        ci.setStartDate(LocalDate.of(2023, 9, 1));
        ci.setEndDate(LocalDate.of(2024, 2, 28));
        completedInternshipsRepository.saveAndFlush(ci);

        List<CompletedInternships> found = completedInternshipsRepository.findByStudent_MatriculationNbr(5555);
        assertFalse(found.isEmpty(), "Expected repository to return at least one internship for matriculation 5555");
        assertEquals(5555, found.get(0).getStudent().getMatriculationNbr());
    }
}
