package com.aspd.backend.controller;

import com.aspd.backend.dto.BaselineAssignmentDto;
import com.aspd.backend.dto.BaselineCaptureRequest;
import com.aspd.backend.service.BaselineService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST controller for managing baseline assignments used in re-optimization.
 */
@RestController
@RequestMapping("/api/baselines")
@RequiredArgsConstructor
public class BaselineController {

    private final BaselineService baselineService;

    /**
     * Capture a baseline snapshot of current assignments.
     * POST /api/baselines/capture
     */
    @PreAuthorize("hasAuthority('EDIT')")
    @PostMapping("/capture")
    public ResponseEntity<Map<String, Object>> captureBaseline(@RequestBody BaselineCaptureRequest request) {
        List<BaselineAssignmentDto> baselines = baselineService.captureBaseline(request);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", String.format("Captured %d baseline assignments", baselines.size()));
        response.put("count", baselines.size());
        response.put("schoolYear", request.getSchoolYear());
        response.put("semester", request.getSemester());
        response.put("baselines", baselines);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get baseline assignments for a specific year and semester.
     * GET /api/baselines?schoolYear=2025&semester=winter
     */
    @PreAuthorize("hasAuthority('VIEW') or hasAuthority('EDIT')")
    @GetMapping
    public ResponseEntity<List<BaselineAssignmentDto>> getBaseline(
            @RequestParam String schoolYear,
            @RequestParam String semester) {
        List<BaselineAssignmentDto> baselines = baselineService.getBaseline(schoolYear, semester);
        return ResponseEntity.ok(baselines);
    }

    /**
     * Get only pinned baseline assignments.
     * GET /api/baselines/pinned?schoolYear=2025&semester=winter
     */
    @PreAuthorize("hasAuthority('VIEW') or hasAuthority('EDIT')")
    @GetMapping("/pinned")
    public ResponseEntity<List<BaselineAssignmentDto>> getPinnedBaselines(
            @RequestParam String schoolYear,
            @RequestParam String semester) {
        List<BaselineAssignmentDto> baselines = baselineService.getPinnedBaselines(schoolYear, semester);
        return ResponseEntity.ok(baselines);
    }

    /**
     * Clear baseline for a specific year and semester.
     * DELETE /api/baselines?schoolYear=2025&semester=winter
     */
    @PreAuthorize("hasAuthority('EDIT')")
    @DeleteMapping
    public ResponseEntity<Map<String, Object>> clearBaseline(
            @RequestParam String schoolYear,
            @RequestParam String semester) {
        long deletedCount = baselineService.clearBaseline(schoolYear, semester);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", String.format("Cleared %d baseline assignments", deletedCount));
        response.put("deletedCount", deletedCount);
        response.put("schoolYear", schoolYear);
        response.put("semester", semester);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Update pinned status of a specific baseline assignment.
     * PATCH /api/baselines/{id}/pinned
     */
    @PreAuthorize("hasAuthority('EDIT')")
    @PatchMapping("/{id}/pinned")
    public ResponseEntity<BaselineAssignmentDto> updatePinnedStatus(
            @PathVariable Long id,
            @RequestBody Map<String, Boolean> body) {
        boolean pinned = body.getOrDefault("pinned", false);
        BaselineAssignmentDto updated = baselineService.updatePinnedStatus(id, pinned);
        return ResponseEntity.ok(updated);
    }

    /**
     * Check if baseline exists for given year and semester.
     * GET /api/baselines/exists?schoolYear=2025&semester=winter
     */
    @PreAuthorize("hasAuthority('VIEW') or hasAuthority('EDIT')")
    @GetMapping("/exists")
    public ResponseEntity<Map<String, Object>> checkBaselineExists(
            @RequestParam String schoolYear,
            @RequestParam String semester) {
        boolean exists = baselineService.baselineExists(schoolYear, semester);
        long count = exists ? baselineService.getBaselineCount(schoolYear, semester) : 0;
        
        Map<String, Object> response = new HashMap<>();
        response.put("exists", exists);
        response.put("count", count);
        response.put("schoolYear", schoolYear);
        response.put("semester", semester);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get all distinct years that have baselines.
     * GET /api/baselines/years
     */
    @PreAuthorize("hasAuthority('VIEW') or hasAuthority('EDIT')")
    @GetMapping("/years")
    public ResponseEntity<List<String>> getAllBaselineYears() {
        List<String> years = baselineService.getAllBaselineYears();
        return ResponseEntity.ok(years);
    }
}
