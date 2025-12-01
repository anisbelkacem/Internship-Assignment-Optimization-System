package com.aspd.backend.repository;

import com.aspd.backend.model.StudentConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StudentConfigRepository extends JpaRepository<StudentConfig, Long> {

    // Find a config by student id and year
    Optional<StudentConfig> findByStudent_MatriculationNbrAndYear(int studentId, String year);

    // List all configs for a specific student
    List<StudentConfig> findByStudent_MatriculationNbr(int studentId);

    // List all configs for a given year
    List<StudentConfig> findByYear(String year);
}
