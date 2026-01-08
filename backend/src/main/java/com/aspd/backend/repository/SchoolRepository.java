package com.aspd.backend.repository;

import com.aspd.backend.model.School;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

public interface SchoolRepository extends JpaRepository<School, Long> {
    Optional<School> findByNameIgnoreCase(String name);
    List<School> findByActiveTrue();
}
