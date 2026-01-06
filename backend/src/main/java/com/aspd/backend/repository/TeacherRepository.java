package com.aspd.backend.repository;

import com.aspd.backend.model.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

import java.util.Optional;
import com.aspd.backend.model.Teacher;

public interface TeacherRepository extends JpaRepository<Teacher, Long> {

    Optional<Teacher> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByFirstNameAndLastNameAndMainSubject(String firstName, String lastName, Course mainSubject);
    
    @Query("SELECT DISTINCT t FROM Teacher t LEFT JOIN FETCH t.plConfigs WHERE t.teacherId IN (SELECT t2.teacherId FROM Teacher t2)")
    List<Teacher> findAllWithConfigs();
}

