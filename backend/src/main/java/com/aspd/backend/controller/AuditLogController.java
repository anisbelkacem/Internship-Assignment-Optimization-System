package com.aspd.backend.controller;

import com.aspd.backend.dto.AuditLogDto;
import com.aspd.backend.dto.UserDto;
import com.aspd.backend.model.AuditLog;
import com.aspd.backend.service.AuditLogService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * REST Controller for audit log operations (read-only)
 */
@RestController
@RequestMapping("/api/audit-logs")
@RequiredArgsConstructor
@Slf4j
public class AuditLogController {

    private final AuditLogService auditLogService;
    private final ObjectMapper objectMapper;

    /**
     * Get audit history for a specific entity
     * GET /api/audit-logs/entity/{entityType}/{entityId}
     */
    @GetMapping("/entity/{entityType}/{entityId}")
    public ResponseEntity<Page<AuditLogDto>> getEntityHistory(
        @PathVariable String entityType,
        @PathVariable Long entityId,
        Pageable pageable
    ) {
        Page<AuditLog> auditLogs = auditLogService.getEntityHistory(entityType, entityId, pageable);
        Page<AuditLogDto> result = convertToDto(auditLogs, pageable);
        return ResponseEntity.ok(result);
    }

    /**
     * Get audit history for a student
     * GET /api/audit-logs/student/{studentId}
     */
    @GetMapping("/student/{studentId}")
    public ResponseEntity<Page<AuditLogDto>> getStudentHistory(
        @PathVariable Long studentId,
        Pageable pageable
    ) {
        Page<AuditLog> auditLogs = auditLogService.getStudentAuditHistory(studentId, pageable);
        Page<AuditLogDto> result = convertToDto(auditLogs, pageable);
        return ResponseEntity.ok(result);
    }

    /**
     * Get audit history for a school
     * GET /api/audit-logs/school/{schoolId}
     */
    @GetMapping("/school/{schoolId}")
    public ResponseEntity<Page<AuditLogDto>> getSchoolHistory(
        @PathVariable Long schoolId,
        Pageable pageable
    ) {
        Page<AuditLog> auditLogs = auditLogService.getSchoolAuditHistory(schoolId, pageable);
        Page<AuditLogDto> result = convertToDto(auditLogs, pageable);
        return ResponseEntity.ok(result);
    }

    /**
     * Get audit history for a school year
     * GET /api/audit-logs/school-year/{schoolYear}
     */
    @GetMapping("/school-year/{schoolYear}")
    public ResponseEntity<Page<AuditLogDto>> getSchoolYearHistory(
        @PathVariable String schoolYear,
        Pageable pageable
    ) {
        Page<AuditLog> auditLogs = auditLogService.getSchoolYearAuditHistory(schoolYear, pageable);
        Page<AuditLogDto> result = convertToDto(auditLogs, pageable);
        return ResponseEntity.ok(result);
    }

    /**
     * Search audit logs with multiple filter criteria
     * GET /api/audit-logs/search
     */
    @GetMapping("/search")
    public ResponseEntity<Page<AuditLogDto>> searchAuditLogs(
        @RequestParam(required = false) String entityType,
        @RequestParam(required = false) Long studentId,
        @RequestParam(required = false) Long schoolId,
        @RequestParam(required = false) String schoolYear,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
        Pageable pageable
    ) {
        Page<AuditLog> auditLogs = auditLogService.searchAuditLogs(
            entityType,
            studentId,
            schoolId,
            schoolYear,
            startDate,
            endDate,
            pageable
        );
        Page<AuditLogDto> result = convertToDto(auditLogs, pageable);
        return ResponseEntity.ok(result);
    }

    /**
     * Convert AuditLog entities to DTOs with JSON parsing
     */
    private Page<AuditLogDto> convertToDto(Page<AuditLog> auditLogs, Pageable pageable) {
        List<AuditLogDto> dtos = auditLogs.getContent().stream()
            .map(this::mapToDto)
            .collect(Collectors.toList());
        return new PageImpl<>(dtos, pageable, auditLogs.getTotalElements());
    }

    /**
     * Map AuditLog to AuditLogDto with JSON parsing
     */
    private AuditLogDto mapToDto(AuditLog auditLog) {
        Map<String, Object> previousValues = null;
        Map<String, Object> newValues = null;

        try {
            if (auditLog.getPreviousValues() != null) {
                previousValues = objectMapper.readValue(auditLog.getPreviousValues(), Map.class);
            }
            if (auditLog.getNewValues() != null) {
                newValues = objectMapper.readValue(auditLog.getNewValues(), Map.class);
            }
        } catch (Exception e) {
            log.warn("Error parsing JSON values for audit log {}", auditLog.getId(), e);
        }

        return AuditLogDto.builder()
            .id(auditLog.getId())
            .entityType(auditLog.getEntityType())
            .entityId(auditLog.getEntityId())
            .createdBy(mapUserToDto(auditLog.getCreatedBy()))
            .timestamp(auditLog.getTimestamp())
            .action(auditLog.getAction().name())
            .previousValues(previousValues)
            .newValues(newValues)
            .description(auditLog.getDescription())
            .relatedStudentId(auditLog.getRelatedStudentId())
            .relatedSchoolId(auditLog.getRelatedSchoolId())
            .schoolYear(auditLog.getSchoolYear())
            .build();
    }

    /**
     * Map User to UserDto (simple version for audit logs)
     */
    private UserDto mapUserToDto(com.aspd.backend.model.User user) {
        if (user == null) {
            return null;
        }
        return UserDto.builder()
            .id(user.getId())
            .firstName(user.getFirstName())
            .lastName(user.getLastName())
            .email(user.getEmail())
            .build();
    }
}
