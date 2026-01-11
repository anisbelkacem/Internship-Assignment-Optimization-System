package com.aspd.backend.repository;

import com.aspd.backend.model.StudentInternshipDemand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudentInternshipDemandRepository extends JpaRepository<StudentInternshipDemand, Long> {
    
    List<StudentInternshipDemand> findBySchoolYear(String schoolYear);
    
    void deleteBySchoolYear(String schoolYear);
}
