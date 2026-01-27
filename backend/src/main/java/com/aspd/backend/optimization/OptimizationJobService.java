package com.aspd.backend.optimization;

import com.aspd.backend.optimization.JobStatus;
import com.aspd.backend.optimization.JobType;
import com.aspd.backend.optimization.OptimizationJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for managing optimization jobs.
 * Uses in-memory ConcurrentHashMap for job storage.
 * Jobs are auto-cleaned after 1 hour of completion.
 */
@Service
@Slf4j
public class OptimizationJobService {
    
    private final Map<String, OptimizationJob> jobs = new ConcurrentHashMap<>();
    
    /**
     * Create a new job or return existing job if one is already running for the same problem.
     * Uses schoolYear + jobType as the unique problem identifier.
     * 
     * @param jobType Type of optimization
     * @param schoolYear School year
     * @param solverTimeLimitSeconds Time limit for solver
     * @return New or existing job
     */
    public OptimizationJob createOrGetExistingJob(JobType jobType, String schoolYear, Integer solverTimeLimitSeconds) {
        // Check if a job is already running for this problem
        String problemId = buildProblemId(jobType, schoolYear);
        Optional<OptimizationJob> existingJob = findRunningJobByProblemId(problemId);
        
        if (existingJob.isPresent()) {
            log.info("Job already running for {}: {}", problemId, existingJob.get().getJobId());
            return existingJob.get();
        }
        
        // Create new job
        String jobId = UUID.randomUUID().toString();
        OptimizationJob job = OptimizationJob.builder()
                .jobId(jobId)
                .jobType(jobType)
                .schoolYear(schoolYear)
                .status(JobStatus.QUEUED)
                .createdAt(LocalDateTime.now())
                .solverTimeLimitSeconds(solverTimeLimitSeconds)
                .message("Job queued")
                .build();
        
        jobs.put(jobId, job);
        log.info("Created new job: {} for {}", jobId, problemId);
        return job;
    }
    
    /**
     * Get job by ID.
     */
    public Optional<OptimizationJob> getJob(String jobId) {
        return Optional.ofNullable(jobs.get(jobId));
    }
    
    /**
     * Get job if it exists for a given job type and school year.
     * Returns null if no job exists.
     */
    public OptimizationJob getJobIfExists(JobType jobType, String schoolYear) {
        String problemId = buildProblemId(jobType, schoolYear);
        return jobs.values().stream()
                .filter(job -> {
                    String jobProblemId = buildProblemId(job.getJobType(), job.getSchoolYear());
                    return jobProblemId.equals(problemId);
                })
                .findFirst()
                .orElse(null);
    }
    
    /**
     * Update job status to RUNNING.
     */
    public void markJobAsRunning(String jobId) {
        OptimizationJob job = jobs.get(jobId);
        if (job != null) {
            job.setStatus(JobStatus.RUNNING);
            job.setStartedAt(LocalDateTime.now());
            job.setMessage("Optimization running");
            log.info("Job {} marked as RUNNING", jobId);
        }
    }
    
    /**
     * Update job status to COMPLETED.
     */
    public void markJobAsCompleted(String jobId, String message) {
        OptimizationJob job = jobs.get(jobId);
        if (job != null) {
            job.setStatus(JobStatus.COMPLETED);
            job.setCompletedAt(LocalDateTime.now());
            job.setMessage(message != null ? message : "Optimization completed");
            log.info("Job {} marked as COMPLETED", jobId);
        }
    }
    
    /**
     * Update job status to FAILED.
     */
    public void markJobAsFailed(String jobId, String errorMessage) {
        OptimizationJob job = jobs.get(jobId);
        if (job != null) {
            job.setStatus(JobStatus.FAILED);
            job.setCompletedAt(LocalDateTime.now());
            job.setErrorMessage(errorMessage);
            job.setMessage("Optimization failed: " + errorMessage);
            log.error("Job {} marked as FAILED: {}", jobId, errorMessage);
        }
    }
    
    /**
     * Clean up completed jobs older than 1 hour.
     * Runs every 10 minutes.
     */
    @Scheduled(fixedDelay = 600000) // 10 minutes
    public void cleanupOldJobs() {
        LocalDateTime cutoff = LocalDateTime.now().minus(1, ChronoUnit.HOURS);
        int removed = 0;
        
        for (Map.Entry<String, OptimizationJob> entry : jobs.entrySet()) {
            OptimizationJob job = entry.getValue();
            if ((job.getStatus() == JobStatus.COMPLETED || job.getStatus() == JobStatus.FAILED) &&
                job.getCompletedAt() != null && job.getCompletedAt().isBefore(cutoff)) {
                jobs.remove(entry.getKey());
                removed++;
            }
        }
        
        if (removed > 0) {
            log.info("Cleaned up {} old jobs", removed);
        }
    }
    
    /**
     * Build unique problem identifier from jobType and schoolYear.
     * Example: "PHASE1-WiSe2025"
     */
    private String buildProblemId(JobType jobType, String schoolYear) {
        return jobType.name() + "-" + schoolYear;
    }
    
    /**
     * Find a running job for the given problem identifier.
     */
    private Optional<OptimizationJob> findRunningJobByProblemId(String problemId) {
        return jobs.values().stream()
                .filter(job -> {
                    String jobProblemId = buildProblemId(job.getJobType(), job.getSchoolYear());
                    return jobProblemId.equals(problemId) && 
                           (job.getStatus() == JobStatus.QUEUED || job.getStatus() == JobStatus.RUNNING);
                })
                .findFirst();
    }
}
