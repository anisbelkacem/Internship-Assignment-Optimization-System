package com.aspd.backend.repository;

import com.aspd.backend.model.InternshipAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface InternshipAssignmentRepository extends JpaRepository<InternshipAssignment, Long> {
    List<InternshipAssignment> findByStudentConfigYear(String year);
    List<InternshipAssignment> findBySchoolYear(String schoolYear);
}