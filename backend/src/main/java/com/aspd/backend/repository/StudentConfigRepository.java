package com.aspd.backend.repository;

import com.aspd.backend.model.StudentConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface StudentConfigRepository extends JpaRepository<StudentConfig, Long> {

    // Find a config by student id and year
    Optional<StudentConfig> findByStudent_MatriculationNbrAndYear(int studentId, String year);

    // List all configs for a specific student
    List<StudentConfig> findByStudent_MatriculationNbr(int studentId);

    // List all configs for a given year
    List<StudentConfig> findByYear(String year);
    
    // List all configs for a given year with eagerly fetched Student and Courses
    @Query("SELECT sc FROM StudentConfig sc " +
            "JOIN FETCH sc.student " +
            "LEFT JOIN FETCH sc.mainCourse " +
            "LEFT JOIN FETCH sc.prefCourse1 " +
            "LEFT JOIN FETCH sc.prefCourse2 " +
            "LEFT JOIN FETCH sc.prefCourse3 " +
            "WHERE sc.year = :year")
    List<StudentConfig> findByYearWithStudent(String year);
    
    @Query("SELECT DISTINCT sc.year FROM StudentConfig sc")
    List<String> findDistinctYears();
}
