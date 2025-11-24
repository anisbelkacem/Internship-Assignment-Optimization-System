package com.aspd.backend.repository;

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

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private SchoolRepository schoolRepository;

    @Test
    @DisplayName("save and find by teacherId and schoolId")
    @Transactional
    void saveAndFindByTeacherAndSchool() {

        Student s = new Student();
        s.setMatriculationNbr(1001);
        s.setFirstName("Test");
        s.setLastName("Student");
        studentRepository.save(s);

        Teacher t = new Teacher();
        t.setFirstName("Teach");
        t.setLastName("Er");
        t.setEmail("teacher@test.com");
        t.setMainSubject(Course.SCIENCES);
        teacherRepository.save(t);

        School school = new School();
        school.setName("Test School");
        school.setAddress("School Str");
        school.setZone("A");
        school.setOepnv(true);
        school.setType(SchoolType.GS);
        schoolRepository.save(school);

        CompletedInternships ci = new CompletedInternships();
        ci.setStudent(s);
        ci.setTeacher(t);
        ci.setSchool(school);
        ci.setCourse(Course.COMPUTER_SCIENCE);
        ci.setStartDate(LocalDate.of(2024, 1, 1));
        ci.setEndDate(LocalDate.of(2024, 6, 30));
        ci.setDescription("Test internship");

        completedInternshipsRepository.save(ci);

        List<CompletedInternships> byTeacher =
                completedInternshipsRepository.findByTeacher_TeacherId(t.getTeacherId());

        assertFalse(byTeacher.isEmpty());
        assertEquals(t.getTeacherId(), byTeacher.get(0).getTeacher().getTeacherId());

        List<CompletedInternships> bySchool =
                completedInternshipsRepository.findBySchool_Id(school.getId());

        assertFalse(bySchool.isEmpty());
        assertEquals(school.getId(), bySchool.get(0).getSchool().getId());
    }

    @Test
    @DisplayName("find by student's matriculation number")
    void findByStudentMatriculationNumber() {

        Student s = new Student();
        s.setMatriculationNbr(5555);
        s.setFirstName("Find");
        s.setLastName("Me");
        studentRepository.saveAndFlush(s);

        Teacher t = new Teacher();
        t.setFirstName("T");
        t.setLastName("T");
        t.setEmail("t@test.com");
        t.setMainSubject(Course.SCIENCES);
        teacherRepository.saveAndFlush(t);

        School school = new School();
        school.setName("School");
        school.setAddress("Addr");
        school.setZone("C");
        school.setOepnv(true);
        school.setType(SchoolType.MS);
        schoolRepository.saveAndFlush(school);

        CompletedInternships ci = new CompletedInternships();
        ci.setStudent(s);
        ci.setTeacher(t);
        ci.setSchool(school);
        ci.setCourse(Course.OTHER);
        ci.setStartDate(LocalDate.of(2023, 9, 1));
        ci.setEndDate(LocalDate.of(2024, 2, 28));
        completedInternshipsRepository.saveAndFlush(ci);

        List<CompletedInternships> found =
                completedInternshipsRepository.findByStudent_MatriculationNbr(5555);

        assertFalse(found.isEmpty());
        assertEquals(5555, found.get(0).getStudent().getMatriculationNbr());
    }
}
