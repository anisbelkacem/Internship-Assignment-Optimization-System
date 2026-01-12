package com.aspd.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLogDto {
    private Long id;
    private String entityType;
    private Long entityId;
    private String action;         // enum name: CREATE, UPDATE, DELETE
    private LocalDateTime timestamp;
    private String description;
    private String previousValues; // JSON string
    private String newValues;      // JSON string
}
