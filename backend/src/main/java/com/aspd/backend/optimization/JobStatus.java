package com.aspd.backend.optimization;

/**
 * Status of an optimization job.
 */
public enum JobStatus {
    /**
     * Job has been created but not yet started.
     */
    QUEUED,
    
    /**
     * Job is currently running.
     */
    RUNNING,
    
    /**
     * Job completed successfully.
     */
    COMPLETED,
    
    /**
     * Job failed with an error.
     */
    FAILED
}
