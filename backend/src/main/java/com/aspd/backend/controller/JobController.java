package com.aspd.backend.controller;

import com.aspd.backend.optimization.OptimizationJob;
import com.aspd.backend.optimization.OptimizationJobService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for querying optimization job status.
 */
@Slf4j
@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
public class JobController {
    
    private final OptimizationJobService jobService;
    
    /**
     * Get job status by ID.
     * 
     * @param jobId Job identifier
     * @return Job with current status
     */
    @PreAuthorize("hasAnyAuthority('VIEW', 'EDIT')")
    @GetMapping("/{jobId}")
    public ResponseEntity<OptimizationJob> getJobStatus(@PathVariable String jobId) {
        log.debug("GET /api/jobs/{}", jobId);
        
        return jobService.getJob(jobId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
