package com.aspd.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import com.aspd.backend.model.Teacher;

public interface TeacherRepository extends JpaRepository<Teacher, Long> {

    Optional<Teacher> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByFirstNameAndLastNameAndMainSubject(String firstName, String lastName, String mainSubject);
}
