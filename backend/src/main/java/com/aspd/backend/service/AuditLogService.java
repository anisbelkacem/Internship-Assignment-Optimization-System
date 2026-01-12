package com.aspd.backend.service;

import com.aspd.backend.model.AuditAction;
import com.aspd.backend.model.AuditLog;
import com.aspd.backend.model.User;
import com.aspd.backend.repository.AuditLogRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Service for managing audit logs.
 * Tracks all changes to critical entities for accountability and traceability.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    /**
     * Log a change to an entity
     */
    public void logChange(
        String entityType,
        Long entityId,
        User createdBy,
        AuditAction action,
        Object previousValues,
        Object newValues,
        String description
    ) {
        logChange(entityType, entityId, createdBy, action, previousValues, newValues, description, null, null, null);
    }

    /**
     * Log a change with additional context for filtering
     */
    public void logChange(
        String entityType,
        Long entityId,
        User createdBy,
        AuditAction action,
        Object previousValues,
        Object newValues,
        String description,
        Long relatedStudentId,
        Long relatedSchoolId,
        String schoolYear
    ) {
        try {
            String previousValuesJson = previousValues != null ? objectMapper.writeValueAsString(previousValues) : null;
            String newValuesJson = newValues != null ? objectMapper.writeValueAsString(newValues) : null;

            AuditLog auditLog = AuditLog.builder()
                .entityType(entityType)
                .entityId(entityId)
                .createdBy(createdBy)
                .timestamp(LocalDateTime.now())
                .action(action)
                .previousValues(previousValuesJson)
                .newValues(newValuesJson)
                .description(description)
                .relatedStudentId(relatedStudentId)
                .relatedSchoolId(relatedSchoolId)
                .schoolYear(schoolYear)
                .build();

            auditLogRepository.save(auditLog);
            log.info("Audit log created: {} - {} (ID: {})", entityType, action, entityId);
        } catch (Exception e) {
            log.error("Error creating audit log for {} ID {}", entityType, entityId, e);
        }
    }

    /**
     * Get audit history for a specific entity
     */
    public Page<AuditLog> getEntityHistory(String entityType, Long entityId, Pageable pageable) {
        return auditLogRepository.findByEntityTypeAndEntityIdOrderByTimestampDesc(entityType, entityId)
            .stream()
            .skip(pageable.getOffset())
            .limit(pageable.getPageSize())
            .reduce(
                new org.springframework.data.domain.PageImpl<AuditLog>(new java.util.ArrayList<>(), pageable, 0),
                (page, log) -> {
                    java.util.List<AuditLog> content = new java.util.ArrayList<>(page.getContent());
                    content.add(log);
                    return new org.springframework.data.domain.PageImpl<>(content, pageable, 
                        auditLogRepository.findByEntityTypeAndEntityIdOrderByTimestampDesc(entityType, entityId).size());
                },
                (page1, page2) -> page1
            );
    }

    /**
     * Search audit logs by multiple criteria
     */
    public Page<AuditLog> searchAuditLogs(
        String entityType,
        Long studentId,
        Long schoolId,
        String schoolYear,
        LocalDateTime startDate,
        LocalDateTime endDate,
        Pageable pageable
    ) {
        return auditLogRepository.findByMultipleCriteria(
            entityType,
            studentId,
            schoolId,
            schoolYear,
            startDate,
            endDate,
            pageable
        );
    }

    /**
     * Get all audit logs for a student
     */
    public Page<AuditLog> getStudentAuditHistory(Long studentId, Pageable pageable) {
        return auditLogRepository.findByRelatedStudentIdOrderByTimestampDesc(studentId, pageable);
    }

    /**
     * Get all audit logs for a school
     */
    public Page<AuditLog> getSchoolAuditHistory(Long schoolId, Pageable pageable) {
        return auditLogRepository.findByRelatedSchoolIdOrderByTimestampDesc(schoolId, pageable);
    }

    /**
     * Get audit logs for a school year
     */
    public Page<AuditLog> getSchoolYearAuditHistory(String schoolYear, Pageable pageable) {
        return auditLogRepository.findBySchoolYearOrderByTimestampDesc(schoolYear, pageable);
    }

    /**
     * Create a map of changed fields for audit logging
     */
    public static Map<String, Object> createChangeMap(String... keyValues) {
        Map<String, Object> map = new HashMap<>();
        for (int i = 0; i < keyValues.length - 1; i += 2) {
            map.put(keyValues[i], keyValues[i + 1]);
        }
        return map;
    }
}
