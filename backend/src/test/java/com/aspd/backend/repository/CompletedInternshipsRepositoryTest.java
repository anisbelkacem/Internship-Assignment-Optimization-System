package com.aspd.backend.repository;

import com.aspd.backend.model.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

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

    @Autowired
    private CourseRepository courseRepository;

    @Test
    @DisplayName("save and find by teacherId and schoolId")
    @Transactional
    void saveAndFindByTeacherAndSchool() {

        Student s = new Student();
        s.setMatriculationNbr(1001);
        s.setFirstName("Test");
        s.setLastName("Student");
        studentRepository.save(s);


        School school = new School();
        school.setName("Test School");
        school.setAddress("School Str");
        school.setZone("A");
        school.setOepnv(OepnvStatus.FOUR_A);
        school.setType(SchoolType.GS);
        schoolRepository.save(school);

        Course sciences = new Course("SCIENCES");
        Course cs = new Course("Computer Science");

        courseRepository.save(sciences);
        courseRepository.save(cs);

        Teacher t = new Teacher();
        t.setFirstName("Teach");
        t.setLastName("Er");
        t.setEmail("teacher@test.com");
        t.setMainSubject(sciences);
        t.setSchool(school);
        teacherRepository.save(t);


        CompletedInternships ci = new CompletedInternships();
        ci.setStudent(s);
        ci.setTeacher(t);
        ci.setSchool(school);
        ci.setCourse(cs);
        ci.setDescription("Test internship");
        ci.setType(PraktikumType.PDP_I);
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


        School school = new School();
        school.setName("School");
        school.setAddress("Addr");
        school.setZone("C");
        school.setOepnv(OepnvStatus.FOUR_A);
        school.setType(SchoolType.MS);
        schoolRepository.saveAndFlush(school);

        Course cs = new Course("Computer Sciences");
        Course other = new Course("Other");

        courseRepository.save(cs);
        courseRepository.save(other);


        Teacher t = new Teacher();
        t.setFirstName("T");
        t.setLastName("T");
        t.setEmail("t@test.com");
        t.setMainSubject(cs);
        t.setSchool(school);
        teacherRepository.saveAndFlush(t);

        CompletedInternships ci = new CompletedInternships();
        ci.setStudent(s);
        ci.setTeacher(t);
        ci.setSchool(school);
        ci.setCourse(other);
        ci.setType(PraktikumType.SFP);
        completedInternshipsRepository.saveAndFlush(ci);

        List<CompletedInternships> found =
                completedInternshipsRepository.findByStudent_MatriculationNbr(5555);

        assertFalse(found.isEmpty());
        assertEquals(5555, found.get(0).getStudent().getMatriculationNbr());
    }
}
