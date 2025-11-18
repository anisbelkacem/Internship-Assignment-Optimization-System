package com.aspd.backend.repository;

import com.aspd.backend.model.CompletedInternships;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CompletedInternshipsRepository extends JpaRepository<CompletedInternships, Long> {
    List<CompletedInternships> findByStudent_MatriculationNbr(int matriculationNbr);
    List<CompletedInternships> findByTeacherId(Long teacherId);
    List<CompletedInternships> findBySchoolId(Long schoolId);
}
