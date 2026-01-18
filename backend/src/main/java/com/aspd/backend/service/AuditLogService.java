package com.aspd.backend.service;

import com.aspd.backend.model.AuditAction;
import com.aspd.backend.model.AuditLog;
import com.aspd.backend.repository.AuditLogRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    /**
     * Log a change: who did what, when, and what changed
     */
    public void log(String entityType, Long entityId, String actionName, String description, 
                    Map<String, Object> previousValues, Map<String, Object> newValues) {
        try {
            String username = getCurrentUsername();
            AuditAction action = AuditAction.valueOf(actionName.toUpperCase());
            
            String previousValuesJson = previousValues != null ? objectMapper.writeValueAsString(previousValues) : null;
            String newValuesJson = newValues != null ? objectMapper.writeValueAsString(newValues) : null;

            AuditLog auditLog = AuditLog.builder()
                    .entityType(entityType)
                    .entityId(entityId)
                    .action(action)
                    .createdBy(null)
                    .createdByUsername(username)
                    .timestamp(LocalDateTime.now())
                    .description(description)
                    .previousValues(previousValuesJson)
                    .newValues(newValuesJson)
                    .build();

            auditLogRepository.save(auditLog);
        } catch (Exception e) {
            System.err.println("Audit logging failed: " + e.getMessage());
        }
    }

    /**
     * Get all audit logs paginated
     */
    public Page<AuditLog> getAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return auditLogRepository.findAllByOrderByTimestampDesc(pageable);
    }

    /**
     * Get audit logs for a specific entity type and ID
     */
    public Page<AuditLog> getEntityHistory(String entityType, Long entityId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return auditLogRepository.findByEntityTypeAndEntityIdOrderByTimestampDesc(entityType, entityId, pageable);
    }

    private String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null && auth.isAuthenticated()) ? auth.getName() : "SYSTEM";
    }
}
