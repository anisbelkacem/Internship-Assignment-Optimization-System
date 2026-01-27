package com.aspd.backend.optimization;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Represents an optimization job with its current status and result.
 * Stored in-memory using ConcurrentHashMap.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OptimizationJob {
    
    /**
     * Unique job identifier (UUID).
     */
    private String jobId;
    
    /**
     * Type of optimization job.
     */
    private JobType jobType;
    
    /**
     * School year for this optimization.
     */
    private String schoolYear;
    
    /**
     * Current status of the job.
     */
    private JobStatus status;
    
    /**
     * When the job was created.
     */
    private LocalDateTime createdAt;
    
    /**
     * When the job started running.
     */
    private LocalDateTime startedAt;
    
    /**
     * When the job completed (success or failure).
     */
    private LocalDateTime completedAt;
    
    /**
     * Error message if job failed.
     */
    private String errorMessage;
    
    /**
     * Solver time limit in seconds (for reference).
     */
    private Integer solverTimeLimitSeconds;
    
    /**
     * Human-readable message about job status.
     */
    private String message;
}
