package com.aspd.backend.repository;

import com.aspd.backend.model.TeacherPlConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TeacherPlConfigRepository extends JpaRepository<TeacherPlConfig, Long> {

    List<TeacherPlConfig> findByTeacher_TeacherId(Long teacherId);

    Optional<TeacherPlConfig> findByTeacher_TeacherIdAndSchoolYear(Long teacherId, String schoolYear);
}
