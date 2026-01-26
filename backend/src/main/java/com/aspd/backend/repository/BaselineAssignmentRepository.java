package com.aspd.backend.repository;

import com.aspd.backend.model.BaselineAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BaselineAssignmentRepository extends JpaRepository<BaselineAssignment, Long> {

    /**
     * Find all baseline assignments for a specific school year (e.g., "WiSe2025", "SoSe2025").
     */
    List<BaselineAssignment> findBySchoolYear(String schoolYear);

    /**
     * Find baseline assignment for a specific student demand in a given year.
     */
    Optional<BaselineAssignment> findByStudentDemandIdAndSchoolYear(
            Long studentDemandId, String schoolYear);

    /**
     * Check if a baseline exists for the given year.
     */
    boolean existsBySchoolYear(String schoolYear);

    /**
     * Find all pinned baseline assignments for a specific year.
     * These are assignments that must not change during re-optimization.
     */
    List<BaselineAssignment> findBySchoolYearAndPinnedTrue(String schoolYear);

    /**
     * Count how many baselines exist for a given year.
     */
    long countBySchoolYear(String schoolYear);

    /**
     * Delete all baseline assignments for a specific year.
     * Used to clear old baselines before creating new ones.
     */
    @Modifying
    @Query("DELETE FROM BaselineAssignment ba WHERE ba.schoolYear = :schoolYear")
    void deleteBySchoolYear(@Param("schoolYear") String schoolYear);

    /**
     * Find all distinct school years that have baselines.
     */
    @Query("SELECT DISTINCT ba.schoolYear FROM BaselineAssignment ba ORDER BY ba.schoolYear")
    List<String> findDistinctSchoolYears();

    /**
     * Find baselines by teacher for a given year.
     * Useful to see which teacher's assignments are in the baseline.
     */
    List<BaselineAssignment> findByTeacherTeacherIdAndSchoolYear(
            Long teacherId, String schoolYear);
}
