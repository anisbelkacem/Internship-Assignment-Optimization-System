package com.aspd.backend.service;

import com.aspd.backend.common.exception.NotFoundException;
import com.aspd.backend.dto.BaselineAssignmentDto;
import com.aspd.backend.dto.BaselineCaptureRequest;
import com.aspd.backend.model.*;
import com.aspd.backend.repository.BaselineAssignmentRepository;
import com.aspd.backend.repository.StudentInternshipDemandRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing baseline assignments used in re-optimization.
 * Captures snapshots of current assignments to preserve valid allocations
 * when re-optimizing for semester changes.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BaselineService {

    private final BaselineAssignmentRepository baselineRepository;
    private final StudentInternshipDemandRepository demandRepository;
    private final AuditLogService auditLogService;

    /**
     * Capture a baseline snapshot of current assignments for a given year.
     * 
     * @param request Contains year (with semester notation like "WiSe2025") and options for capturing baseline
     * @return List of captured baseline assignments
     * @throws IllegalStateException if baseline already exists and overwrite is false
     */
    @Transactional
    public List<BaselineAssignmentDto> captureBaseline(BaselineCaptureRequest request) {
        String year = request.getSchoolYear();
        
        log.info("Capturing baseline for year={}", year);
        
        // Check if baseline already exists
        boolean exists = baselineRepository.existsBySchoolYear(year);
        if (exists && !request.isOverwriteExisting()) {
            throw new IllegalStateException(
                String.format("Baseline already exists for year=%s. Set overwriteExisting=true to replace it.", year)
            );
        }
        
        // Clear existing baseline if overwriting
        if (exists) {
            log.info("Clearing existing baseline before overwriting");
            baselineRepository.deleteBySchoolYear(year);
        }
        
        // Get all student demands for this year that have assignments
        List<StudentInternshipDemand> demands = demandRepository.findBySchoolYear(year);
        
        // Filter to only those with assignments
        List<StudentInternshipDemand> assignedDemands = demands.stream()
            .filter(d -> d.getAssignedInternship() != null)
            .collect(Collectors.toList());
        
        // Further filter by specific demand IDs if provided
        if (request.getStudentDemandIds() != null && !request.getStudentDemandIds().isEmpty()) {
            assignedDemands = assignedDemands.stream()
                .filter(d -> request.getStudentDemandIds().contains(d.getId()))
                .collect(Collectors.toList());
        }
        
        log.info("Found {} assigned demands to capture", assignedDemands.size());
        
        // Get current user for audit
        String createdBy = getCurrentUsername();
        LocalDateTime now = LocalDateTime.now();
        
        // Create baseline assignments
        List<BaselineAssignment> baselineAssignments = new ArrayList<>();
        for (StudentInternshipDemand demand : assignedDemands) {
            PlannedInternship internship = demand.getAssignedInternship();
            
            // Validate that internship has teacher and school
            if (internship.getAssignedTeacher() == null || internship.getAssignedSchool() == null) {
                log.warn("Skipping demand {} - internship {} missing teacher or school", 
                    demand.getId(), internship.getId());
                continue;
            }
            
            BaselineAssignment baseline = BaselineAssignment.builder()
                .studentDemand(demand)
                .plannedInternship(internship)
                .teacher(internship.getAssignedTeacher())
                .school(internship.getAssignedSchool())
                .schoolYear(year)
                .capturedAt(now)
                .pinned(false) // Default to not pinned, can be updated later
                .notes(request.getNotes())
                .createdBy(createdBy)
                .build();
            
            baselineAssignments.add(baseline);
        }
        
        // Save all baselines
        List<BaselineAssignment> saved = baselineRepository.saveAll(baselineAssignments);
        
        log.info("Captured {} baseline assignments for year={}", saved.size(), year);
        
        // Log the baseline capture event
        auditLogService.log(
            "BaselineAssignment",
            null,
            "CAPTURE",
            String.format("Captured baseline: %d assignments for year=%s", saved.size(), year),
            null,
            null
        );
        
        return saved.stream()
            .map(this::toDto)
            .collect(Collectors.toList());
    }

    /**
     * Get baseline assignments for a specific year.
     * 
     * @param schoolYear The school year (e.g., "WiSe2025", "SoSe2025")
     * @return List of baseline assignments
     */
    @Transactional(readOnly = true)
    public List<BaselineAssignmentDto> getBaseline(String schoolYear) {
        log.info("Retrieving baseline for year={}", schoolYear);
        
        List<BaselineAssignment> baselines = baselineRepository.findBySchoolYear(schoolYear);
        
        return baselines.stream()
            .map(this::toDto)
            .collect(Collectors.toList());
    }

    /**
     * Get all pinned baseline assignments for a year.
     * These are assignments that must not change during re-optimization.
     */
    @Transactional(readOnly = true)
    public List<BaselineAssignmentDto> getPinnedBaselines(String schoolYear) {
        log.info("Retrieving pinned baselines for year={}", schoolYear);
        
        List<BaselineAssignment> baselines = baselineRepository
            .findBySchoolYearAndPinnedTrue(schoolYear);
        
        return baselines.stream()
            .map(this::toDto)
            .collect(Collectors.toList());
    }

    /**
     * Clear baseline for a specific year.
     * 
     * @param schoolYear The school year
     * @return Number of baselines deleted
     */
    @Transactional
    public long clearBaseline(String schoolYear) {
        log.info("Clearing baseline for year={}", schoolYear);
        
        long count = baselineRepository.countBySchoolYear(schoolYear);
        
        if (count == 0) {
            log.warn("No baseline found for year={}", schoolYear);
            return 0;
        }
        
        baselineRepository.deleteBySchoolYear(schoolYear);
        
        log.info("Deleted {} baseline assignments for year={}", count, schoolYear);
        
        // Log the baseline deletion
        auditLogService.log(
            "BaselineAssignment",
            null,
            "DELETE",
            String.format("Cleared baseline: %d assignments for year=%s", count, schoolYear),
            null,
            null
        );
        
        return count;
    }

    /**
     * Update the pinned status of a baseline assignment.
     * Pinned assignments will not be changed during re-optimization.
     */
    @Transactional
    public BaselineAssignmentDto updatePinnedStatus(Long baselineId, boolean pinned) {
        BaselineAssignment baseline = baselineRepository.findById(baselineId)
            .orElseThrow(() -> new NotFoundException("BaselineAssignment", baselineId));
        
        baseline.setPinned(pinned);
        BaselineAssignment saved = baselineRepository.save(baseline);
        
        log.info("Updated baseline {} pinned status to {}", baselineId, pinned);
        
        return toDto(saved);
    }

    /**
     * Check if a baseline exists for the given year.
     */
    @Transactional(readOnly = true)
    public boolean baselineExists(String schoolYear) {
        return baselineRepository.existsBySchoolYear(schoolYear);
    }

    /**
     * Get count of baseline assignments for a year.
     */
    @Transactional(readOnly = true)
    public long getBaselineCount(String schoolYear) {
        return baselineRepository.countBySchoolYear(schoolYear);
    }

    /**
     * Get all distinct years that have baselines.
     */
    @Transactional(readOnly = true)
    public List<String> getAllBaselineYears() {
        return baselineRepository.findDistinctSchoolYears();
    }

    // Helper methods

    private BaselineAssignmentDto toDto(BaselineAssignment baseline) {
        Student student = baseline.getStudentDemand().getStudentConfig().getStudent();
        
        return BaselineAssignmentDto.builder()
            .id(baseline.getId())
            .studentDemandId(baseline.getStudentDemand().getId())
            .studentMatriculationNbr(student.getMatriculationNbr())
            .studentName(student.getFirstName() + " " + student.getLastName())
            .praktikumType(baseline.getStudentDemand().getPraktikumType().toString())
            .plannedInternshipId(baseline.getPlannedInternship().getId())
            .teacherId(baseline.getTeacher().getTeacherId())
            .teacherName(baseline.getTeacher().getFirstName() + " " + baseline.getTeacher().getLastName())
            .schoolId(baseline.getSchool().getId())
            .schoolName(baseline.getSchool().getName())
            .schoolYear(baseline.getSchoolYear())
            .capturedAt(baseline.getCapturedAt())
            .pinned(baseline.isPinned())
            .notes(baseline.getNotes())
            .createdBy(baseline.getCreatedBy())
            .build();
    }

    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getName();
        }
        return "system";
    }
}
