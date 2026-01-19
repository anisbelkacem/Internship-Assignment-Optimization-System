package com.aspd.backend.controller;

import com.aspd.backend.dto.AuditLogDto;
import com.aspd.backend.model.AuditLog;
import com.aspd.backend.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/audit-logs")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditLogService auditLogService;

    @GetMapping
    public ResponseEntity<Page<AuditLogDto>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<AuditLog> auditLogs = auditLogService.getAll(page, size);
        return ResponseEntity.ok(auditLogs.map(this::convertToDto));
    }

    @GetMapping("/{entityType}/{entityId}")
    public ResponseEntity<Page<AuditLogDto>> getEntityHistory(
            @PathVariable String entityType,
            @PathVariable Long entityId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<AuditLog> auditLogs = auditLogService.getEntityHistory(entityType, entityId, page, size);
        return ResponseEntity.ok(auditLogs.map(this::convertToDto));
    }

    private AuditLogDto convertToDto(AuditLog auditLog) {
        return AuditLogDto.builder()
                .id(auditLog.getId())
                .entityType(auditLog.getEntityType())
                .entityId(auditLog.getEntityId())
                .action(auditLog.getAction().name())
                .createdBy(auditLog.getCreatedByUsername())
                .timestamp(auditLog.getTimestamp())
                .description(auditLog.getDescription())
                .previousValues(auditLog.getPreviousValues())
                .newValues(auditLog.getNewValues())
                .build();
    }
}
