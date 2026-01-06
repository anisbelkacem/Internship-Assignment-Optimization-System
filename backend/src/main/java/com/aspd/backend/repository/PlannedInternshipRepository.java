package com.aspd.backend.repository;

import com.aspd.backend.model.PlannedInternship;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlannedInternshipRepository extends JpaRepository<PlannedInternship, Long> {
    List<PlannedInternship> findBySchoolYear(String schoolYear);
}
