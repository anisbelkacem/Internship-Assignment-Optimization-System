package com.aspd.backend.repository;

import com.aspd.backend.model.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    List<Course> findByActiveTrue();
    boolean existsByName(String name);
    Optional<Course> findByNameIgnoreCase(String name);
}
