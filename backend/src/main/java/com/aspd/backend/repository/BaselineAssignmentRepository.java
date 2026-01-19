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
     * Find all baseline assignments for a specific school year and semester.
     */
    List<BaselineAssignment> findBySchoolYearAndSemester(String schoolYear, String semester);

    /**
     * Find baseline assignment for a specific student demand in a given year/semester.
     */
    Optional<BaselineAssignment> findByStudentDemandIdAndSchoolYearAndSemester(
            Long studentDemandId, String schoolYear, String semester);

    /**
     * Check if a baseline exists for the given year and semester.
     */
    boolean existsBySchoolYearAndSemester(String schoolYear, String semester);

    /**
     * Find all pinned baseline assignments for a specific year and semester.
     * These are assignments that must not change during re-optimization.
     */
    List<BaselineAssignment> findBySchoolYearAndSemesterAndPinnedTrue(String schoolYear, String semester);

    /**
     * Count how many baselines exist for a given year/semester.
     */
    long countBySchoolYearAndSemester(String schoolYear, String semester);

    /**
     * Delete all baseline assignments for a specific year and semester.
     * Used to clear old baselines before creating new ones.
     */
    @Modifying
    @Query("DELETE FROM BaselineAssignment ba WHERE ba.schoolYear = :schoolYear AND ba.semester = :semester")
    void deleteBySchoolYearAndSemester(@Param("schoolYear") String schoolYear, @Param("semester") String semester);

    /**
     * Find all distinct school years that have baselines.
     */
    @Query("SELECT DISTINCT ba.schoolYear FROM BaselineAssignment ba ORDER BY ba.schoolYear")
    List<String> findDistinctSchoolYears();

    /**
     * Find all baselines for a specific school year (all semesters).
     */
    List<BaselineAssignment> findBySchoolYear(String schoolYear);

    /**
     * Find baselines by teacher for a given year/semester.
     * Useful to see which teacher's assignments are in the baseline.
     */
    List<BaselineAssignment> findByTeacherTeacherIdAndSchoolYearAndSemester(
            Long teacherId, String schoolYear, String semester);
}
