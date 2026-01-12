package com.aspd.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

/**
 * Audit log entity for tracking all changes to critical entities.
 * This entity is immutable (append-only) and provides complete traceability.
 */
@Entity
@Table(name = "audit_logs", indexes = {
    @Index(name = "idx_entity_id", columnList = "entity_id"),
    @Index(name = "idx_entity_type", columnList = "entity_type"),
    @Index(name = "idx_created_by", columnList = "created_by_id"),
    @Index(name = "idx_timestamp", columnList = "timestamp"),
    @Index(name = "idx_related_student_id", columnList = "related_student_id"),
    @Index(name = "idx_related_school_id", columnList = "related_school_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // What entity was modified
    @Column(nullable = false)
    private String entityType; // e.g., "InternshipAssignment", "PlannedInternship"

    @Column(nullable = false)
    private Long entityId; // The ID of the modified entity

    // Who made the change
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "created_by_id", nullable = false)
    private User createdBy;

    // When the change happened
    @Column(nullable = false)
    private LocalDateTime timestamp;

    // What action was performed
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuditAction action; // CREATE, UPDATE, DELETE

    // Optional: denormalized data for quick filtering
    private Long relatedStudentId; // For student-related changes
    private Long relatedSchoolId;  // For school-related changes
    private String schoolYear;      // For filtering by academic year

    // The actual changes (JSON format for flexibility)
    @Column(columnDefinition = "LONGTEXT")
    private String previousValues; // JSON string of old values

    @Column(columnDefinition = "LONGTEXT")
    private String newValues; // JSON string of new values

    // Additional context
    @Column(columnDefinition = "TEXT")
    private String description; // Human-readable description of the change
}
