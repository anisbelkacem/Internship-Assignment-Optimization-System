package com.aspd.backend.controller;

import com.aspd.backend.service.SemesterConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * REST API for managing semester configurations.
 * Handles copying and transforming configs between semesters.
 */
@RestController
@RequestMapping("/api/semester-config")
public class SemesterConfigController {

    @Autowired
    private SemesterConfigService semesterConfigService;

    /**
     * Copies all winter semester configurations to summer semester.
     * 
     * Teacher configs are copied as-is.
     * Student configs are transformed:
     * - PDP_I requirement removed (pdpi = 0)
     * - ZSP requirement removed (zsp = 0)
     * - PDP_II and SFP requirements preserved
     * 
     * This reflects the typical semester progression where students
     * complete basic internships (PDP_I, ZSP) in winter and advance
     * to specialized internships (PDP_II, SFP) in summer.
     * 
     * @param sourceYear The source semester (e.g., "WiSe2025")
     * @param targetYear The target semester (e.g., "SoSe2025")
     * @return Summary of copied configurations
     */
    @PostMapping("/copy-to-next-semester")
    public ResponseEntity<Map<String, Object>> copyToNextSemester(
            @RequestParam String sourceYear,
            @RequestParam String targetYear) {
        
        Map<String, Object> result = semesterConfigService.copyConfigsToNextSemester(sourceYear, targetYear);
        
        return ResponseEntity.ok(result);
    }

    /**
     * Deletes all configurations for a specific semester.
     * Use with caution - this will remove all teacher and student configs.
     * 
     * @param schoolYear The semester to delete (e.g., "SoSe2025")
     * @return Summary of deleted records
     */
    @DeleteMapping("/delete-semester")
    public ResponseEntity<Map<String, Object>> deleteSemesterConfigs(
            @RequestParam String schoolYear) {
        
        Map<String, Object> result = semesterConfigService.deleteSemesterConfigs(schoolYear);
        
        return ResponseEntity.ok(result);
    }
}
