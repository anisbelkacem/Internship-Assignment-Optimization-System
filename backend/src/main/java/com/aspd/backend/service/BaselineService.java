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
     * Capture a baseline snapshot of current assignments for a given year and semester.
     * 
     * @param request Contains year, semester, and options for capturing baseline
     * @return List of captured baseline assignments
     * @throws IllegalStateException if baseline already exists and overwrite is false
     */
    @Transactional
    public List<BaselineAssignmentDto> captureBaseline(BaselineCaptureRequest request) {
        String year = request.getSchoolYear();
        String semester = request.getSemester();
        
        log.info("Capturing baseline for year={}, semester={}", year, semester);
        
        // Check if baseline already exists
        boolean exists = baselineRepository.existsBySchoolYearAndSemester(year, semester);
        if (exists && !request.isOverwriteExisting()) {
            throw new IllegalStateException(
                String.format("Baseline already exists for year=%s, semester=%s. Set overwriteExisting=true to replace it.", 
                    year, semester)
            );
        }
        
        // Clear existing baseline if overwriting
        if (exists) {
            log.info("Clearing existing baseline before overwriting");
            baselineRepository.deleteBySchoolYearAndSemester(year, semester);
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
                .semester(semester)
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
        
        log.info("Captured {} baseline assignments for year={}, semester={}", 
            saved.size(), year, semester);
        
        // Log the baseline capture event
        auditLogService.log(
            "BaselineAssignment",
            null,
            "CAPTURE",
            String.format("Captured baseline: %d assignments for year=%s, semester=%s", 
                saved.size(), year, semester),
            null,
            null
        );
        
        return saved.stream()
            .map(this::toDto)
            .collect(Collectors.toList());
    }

    /**
     * Get baseline assignments for a specific year and semester.
     * 
     * @param schoolYear The school year
     * @param semester The semester
     * @return List of baseline assignments
     */
    @Transactional(readOnly = true)
    public List<BaselineAssignmentDto> getBaseline(String schoolYear, String semester) {
        log.info("Retrieving baseline for year={}, semester={}", schoolYear, semester);
        
        List<BaselineAssignment> baselines = baselineRepository.findBySchoolYearAndSemester(
            schoolYear, semester);
        
        return baselines.stream()
            .map(this::toDto)
            .collect(Collectors.toList());
    }

    /**
     * Get all pinned baseline assignments for a year and semester.
     * These are assignments that must not change during re-optimization.
     */
    @Transactional(readOnly = true)
    public List<BaselineAssignmentDto> getPinnedBaselines(String schoolYear, String semester) {
        log.info("Retrieving pinned baselines for year={}, semester={}", schoolYear, semester);
        
        List<BaselineAssignment> baselines = baselineRepository
            .findBySchoolYearAndSemesterAndPinnedTrue(schoolYear, semester);
        
        return baselines.stream()
            .map(this::toDto)
            .collect(Collectors.toList());
    }

    /**
     * Clear baseline for a specific year and semester.
     * 
     * @param schoolYear The school year
     * @param semester The semester
     * @return Number of baselines deleted
     */
    @Transactional
    public long clearBaseline(String schoolYear, String semester) {
        log.info("Clearing baseline for year={}, semester={}", schoolYear, semester);
        
        long count = baselineRepository.countBySchoolYearAndSemester(schoolYear, semester);
        
        if (count == 0) {
            log.warn("No baseline found for year={}, semester={}", schoolYear, semester);
            return 0;
        }
        
        baselineRepository.deleteBySchoolYearAndSemester(schoolYear, semester);
        
        log.info("Deleted {} baseline assignments for year={}, semester={}", 
            count, schoolYear, semester);
        
        // Log the baseline deletion
        auditLogService.log(
            "BaselineAssignment",
            null,
            "DELETE",
            String.format("Cleared baseline: %d assignments for year=%s, semester=%s", 
                count, schoolYear, semester),
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
     * Check if a baseline exists for the given year and semester.
     */
    @Transactional(readOnly = true)
    public boolean baselineExists(String schoolYear, String semester) {
        return baselineRepository.existsBySchoolYearAndSemester(schoolYear, semester);
    }

    /**
     * Get count of baseline assignments for a year and semester.
     */
    @Transactional(readOnly = true)
    public long getBaselineCount(String schoolYear, String semester) {
        return baselineRepository.countBySchoolYearAndSemester(schoolYear, semester);
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
            .semester(baseline.getSemester())
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
