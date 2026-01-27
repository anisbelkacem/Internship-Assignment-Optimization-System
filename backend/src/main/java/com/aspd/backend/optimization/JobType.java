package com.aspd.backend.optimization;

/**
 * Type of optimization job.
 */
public enum JobType {
    /**
     * Phase 1: Teacher and school assignment to internships.
     */
    PHASE1,
    
    /**
     * Phase 2: Student assignment to internships.
     */
    PHASE2,
    
    /**
     * Reoptimization: Combined Phase 1 and Phase 2 for new semester.
     */
    REOPTIMIZATION
}
