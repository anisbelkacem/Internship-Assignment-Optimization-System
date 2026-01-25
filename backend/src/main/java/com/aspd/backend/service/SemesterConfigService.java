package com.aspd.backend.service;

import com.aspd.backend.model.StudentConfig;
import com.aspd.backend.model.TeacherPlConfig;
import com.aspd.backend.repository.StudentConfigRepository;
import com.aspd.backend.repository.TeacherPlConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service for managing semester configurations.
 * Handles copying and transforming configs between semesters.
 */
@Service
public class SemesterConfigService {

    @Autowired
    private TeacherPlConfigRepository teacherPlConfigRepository;

    @Autowired
    private StudentConfigRepository studentConfigRepository;

    /**
     * Copies configurations from source semester to target semester.
     * 
     * Teacher configs: Copied as-is
     * Student configs: Transformed to remove PDP_I and ZSP requirements
     * 
     * @param sourceYear Source semester (e.g., "WiSe2025")
     * @param targetYear Target semester (e.g., "SoSe2025")
     * @return Summary of operation
     */
    @Transactional
    public Map<String, Object> copyConfigsToNextSemester(String sourceYear, String targetYear) {
        Map<String, Object> result = new HashMap<>();
        
        // 1. Delete existing target semester configs (if any)
        deleteSemesterConfigs(targetYear);
        // 2. Copy teacher configs as-is
        List<TeacherPlConfig> sourceTeacherConfigs = teacherPlConfigRepository.findAll().stream()
                .filter(config -> sourceYear.equals(config.getSchoolYear()))
                .collect(Collectors.toList());
        int teachersCopied = 0;
        
        for (TeacherPlConfig sourceConfig : sourceTeacherConfigs) {
            TeacherPlConfig newConfig = new TeacherPlConfig();
            newConfig.setTeacher(sourceConfig.getTeacher());
            newConfig.setSchoolYear(targetYear);
            newConfig.setMaxPraktikaPerYear(sourceConfig.getMaxPraktikaPerYear());
            newConfig.setTotalHoursCredit(sourceConfig.getTotalHoursCredit());
            newConfig.setActive(sourceConfig.isActive());
            
            // Copy collections by creating new Sets
            newConfig.setSubjectSpecializations(new java.util.HashSet<>(sourceConfig.getSubjectSpecializations()));
            newConfig.setInternshipPreferences(new java.util.HashSet<>(sourceConfig.getInternshipPreferences()));
            
            teacherPlConfigRepository.save(newConfig);
            teachersCopied++;
        }
        
        // 3. Copy student configs with PDP_I and ZSP removed
        List<StudentConfig> sourceStudentConfigs = studentConfigRepository.findByYear(sourceYear);
        int studentsCopied = 0;
        
        for (StudentConfig sourceConfig : sourceStudentConfigs) {
            StudentConfig newConfig = new StudentConfig();
            newConfig.setStudent(sourceConfig.getStudent());
            newConfig.setYear(targetYear);
            newConfig.setMainCourse(sourceConfig.getMainCourse());
            newConfig.setPrefCourse1(sourceConfig.getPrefCourse1());
            newConfig.setPrefCourse2(sourceConfig.getPrefCourse2());
            newConfig.setPrefCourse3(sourceConfig.getPrefCourse3());
            newConfig.setSchoolType(sourceConfig.getSchoolType());
            
            // Transform internship requirements: remove PDP_I and ZSP
            newConfig.setPdpI(false);  // Remove PDP_I requirement
            newConfig.setZsp(false);   // Remove ZSP requirement
            newConfig.setPdpII(sourceConfig.isPdpII());  // Keep PDP_II
            newConfig.setSfp(sourceConfig.isSfp());      // Keep SFP
            
            // Only add student if they have at least one internship requirement in summer
            if (newConfig.isPdpII() || newConfig.isSfp()) {
                studentConfigRepository.save(newConfig);
                studentsCopied++;
            }
        }
        
        result.put("sourceYear", sourceYear);
        result.put("targetYear", targetYear);
        result.put("teacherConfigsCopied", teachersCopied);
        result.put("studentConfigsCopied", studentsCopied);
        result.put("transformationApplied", "PDP_I and ZSP requirements removed from student configs");
        
        return result;
    }

    /**
     * Deletes all configurations for a specific semester.
     * 
     * @param schoolYear The semester to delete
     * @return Summary of deletions
     */
    @Transactional
    public Map<String, Object> deleteSemesterConfigs(String schoolYear) {
        Map<String, Object> result = new HashMap<>();
        
        // Delete student configs
        List<StudentConfig> studentConfigs = studentConfigRepository.findByYear(schoolYear);
        int studentsDeleted = studentConfigs.size();
        studentConfigRepository.deleteAll(studentConfigs);
        
        // Delete teacher configs
        List<TeacherPlConfig> teacherConfigs = teacherPlConfigRepository.findAll().stream()
                .filter(config -> schoolYear.equals(config.getSchoolYear()))
                .collect(Collectors.toList());
        int teachersDeleted = teacherConfigs.size();
        teacherPlConfigRepository.deleteAll(teacherConfigs);
        
        result.put("schoolYear", schoolYear);
        result.put("teacherConfigsDeleted", teachersDeleted);
        result.put("studentConfigsDeleted", studentsDeleted);
        
        return result;
    }
}
