package com.aspd.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * DTO for AuditLog - read-only view for frontend
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLogDto {

    private Long id;
    private String entityType;
    private Long entityId;
    private UserDto createdBy;
    private LocalDateTime timestamp;
    private String action;
    private Map<String, Object> previousValues;
    private Map<String, Object> newValues;
    private String description;
    private Long relatedStudentId;
    private Long relatedSchoolId;
    private String schoolYear;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public boolean isReadOnly() {
        return true;
    }
}
