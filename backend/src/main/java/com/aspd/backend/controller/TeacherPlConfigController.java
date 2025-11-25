package com.aspd.backend.controller;

import com.aspd.backend.dto.TeacherPlConfigDto;
import com.aspd.backend.dto.TeacherPlConfigRequest;
import com.aspd.backend.service.TeacherPlConfigService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pls/{teacherId}/pl-configs")
public class TeacherPlConfigController {

    private final TeacherPlConfigService plConfigService;

    public TeacherPlConfigController(TeacherPlConfigService plConfigService) {
        this.plConfigService = plConfigService;
    }

    @PreAuthorize("hasAnyAuthority('VIEW')")
    @GetMapping
    public List<TeacherPlConfigDto> getForTeacher(@PathVariable Long teacherId) {
        return plConfigService.getForTeacher(teacherId);
    }

    @PreAuthorize("hasAnyAuthority('EDIT')")
    @PostMapping
    public TeacherPlConfigDto create(@PathVariable Long teacherId,
                                     @RequestBody TeacherPlConfigRequest request) {
        return plConfigService.create(teacherId, request);
    }

    @PreAuthorize("hasAnyAuthority('EDIT')")
    @PutMapping("/{configId}")
    public TeacherPlConfigDto update(@PathVariable Long teacherId,
                                     @PathVariable Long configId,
                                     @RequestBody TeacherPlConfigRequest request) {
        return plConfigService.update(configId, request);
    }

    @PreAuthorize("hasAnyAuthority('EDIT')")
    @DeleteMapping("/{configId}")
    public void delete(@PathVariable Long teacherId,
                       @PathVariable Long configId) {
        plConfigService.delete(configId);
    }
}
