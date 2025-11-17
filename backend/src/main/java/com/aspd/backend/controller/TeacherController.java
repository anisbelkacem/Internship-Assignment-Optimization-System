package com.aspd.backend.controller;


import com.aspd.backend.dto.TeacherDto;
import com.aspd.backend.dto.TeacherRequest;
import com.aspd.backend.service.TeacherService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pls")
public class TeacherController {

    private final TeacherService teacherService;

    public TeacherController(TeacherService teacherService) {
        this.teacherService = teacherService;
    }

    // List all PLs
    @GetMapping
    public List<TeacherDto> listAll() {
        return teacherService.getAll();
    }

    // Get one PL by id
    @GetMapping("/{id}")
    public TeacherDto getOne(@PathVariable Long id) {
        return teacherService.getById(id);
    }

    // Create a PL
    @PostMapping
    public TeacherDto create(@RequestBody TeacherRequest request) {
        return teacherService.create(request);
    }

    // Update a PL
    @PutMapping("/{id}")
    public TeacherDto update(@PathVariable Long id, @RequestBody TeacherRequest request) {
        return teacherService.update(id, request);
    }

    // Delete a PL
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        teacherService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
