package com.aspd.backend.repository;

import com.aspd.backend.model.CompletedInternships;
import com.aspd.backend.model.Student;
import com.aspd.backend.model.Teacher;
import com.aspd.backend.model.School;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CompletedInternshipsRepository extends JpaRepository<CompletedInternships, Long> {

    // Query by student's matriculation number
    List<CompletedInternships> findByStudent_MatriculationNbr(int matriculationNbr);

    // Query by teacher entity
    List<CompletedInternships> findByTeacher(Teacher teacher);
    // Query by teacher's ID (field name: teacherId)
    List<CompletedInternships> findByTeacher_TeacherId(Long teacherId);

    // Query by school entity
    List<CompletedInternships> findBySchool(School school);
    // Query by school's ID (field name: id)
    List<CompletedInternships> findBySchool_Id(Long schoolId);
}
