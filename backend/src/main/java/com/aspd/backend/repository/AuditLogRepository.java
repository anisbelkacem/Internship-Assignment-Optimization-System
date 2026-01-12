package com.aspd.backend.repository;

import com.aspd.backend.model.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    // Find all audit logs for a specific entity
    List<AuditLog> findByEntityTypeAndEntityIdOrderByTimestampDesc(String entityType, Long entityId);

    // Find all audit logs for an entity type with pagination
    Page<AuditLog> findByEntityTypeOrderByTimestampDesc(String entityType, Pageable pageable);

    // Find audit logs by student with pagination
    Page<AuditLog> findByRelatedStudentIdOrderByTimestampDesc(Long studentId, Pageable pageable);

    // Find audit logs by school with pagination
    Page<AuditLog> findByRelatedSchoolIdOrderByTimestampDesc(Long schoolId, Pageable pageable);

    // Find audit logs by school year
    Page<AuditLog> findBySchoolYearOrderByTimestampDesc(String schoolYear, Pageable pageable);

    // Complex query for filtering by multiple criteria
    @Query("SELECT a FROM AuditLog a WHERE " +
           "(:entityType IS NULL OR a.entityType = :entityType) AND " +
           "(:studentId IS NULL OR a.relatedStudentId = :studentId) AND " +
           "(:schoolId IS NULL OR a.relatedSchoolId = :schoolId) AND " +
           "(:schoolYear IS NULL OR a.schoolYear = :schoolYear) AND " +
           "(:startDate IS NULL OR a.timestamp >= :startDate) AND " +
           "(:endDate IS NULL OR a.timestamp <= :endDate) " +
           "ORDER BY a.timestamp DESC")
    Page<AuditLog> findByMultipleCriteria(
        @Param("entityType") String entityType,
        @Param("studentId") Long studentId,
        @Param("schoolId") Long schoolId,
        @Param("schoolYear") String schoolYear,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        Pageable pageable
    );

    // Search by description
    @Query("SELECT a FROM AuditLog a WHERE a.description LIKE %:searchTerm% ORDER BY a.timestamp DESC")
    Page<AuditLog> searchByDescription(@Param("searchTerm") String searchTerm, Pageable pageable);
}
