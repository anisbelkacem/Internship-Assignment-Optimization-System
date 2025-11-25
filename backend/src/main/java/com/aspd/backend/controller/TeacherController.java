package com.aspd.backend.controller;


import com.aspd.backend.dto.TeacherDto;
import com.aspd.backend.dto.TeacherRequest;
import com.aspd.backend.service.TeacherService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import com.aspd.backend.common.exception.InvalidDataException;

import com.aspd.backend.dto.TeacherImportResult;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/pls")
public class TeacherController {

    private final TeacherService teacherService;

    public TeacherController(TeacherService teacherService) {
        this.teacherService = teacherService;
    }

    // List all PLs
    @PreAuthorize("hasAuthority('VIEW') or hasAnyAuthority('EDIT')")
    @GetMapping
    public List<TeacherDto> listAll() {
        return teacherService.getAll();
    }

    // Get one PL by id
    @PreAuthorize("hasAuthority('VIEW') or hasAnyAuthority('EDIT')")
    @GetMapping("/{id}")
    public TeacherDto getOne(@PathVariable Long id) {
        return teacherService.getById(id);
    }

    // Create a PL
    @PreAuthorize("hasAnyAuthority('EDIT')")
    @PostMapping
    public TeacherDto create(@RequestBody TeacherRequest request) {
        return teacherService.create(request);
    }

    // Update a PL
    @PreAuthorize("hasAnyAuthority('EDIT')")
    @PutMapping("/{id}")
    public TeacherDto update(@PathVariable Long id, @RequestBody TeacherRequest request) {
        return teacherService.update(id, request);
    }

    // Delete a PL
    @PreAuthorize("hasAnyAuthority('EDIT')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        teacherService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // -------------------------------------------------------------
    // Excel IMPORT / EXPORT endpoints  (similar to School)
    // -------------------------------------------------------------

    @PreAuthorize("hasAnyAuthority('EDIT')")
    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public TeacherImportResult importExcel(@RequestPart("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new InvalidDataException("file", null, "is required");
        }
        if (!file.getOriginalFilename().toLowerCase().endsWith(".xlsx")) {
            throw new InvalidDataException("file", file.getOriginalFilename(), "Only .xlsx files are supported");
        }
        return teacherService.importFromExcel(file.getInputStream());
    }

    @PreAuthorize("hasAuthority('VIEW') or hasAnyAuthority('EDIT')")
    @GetMapping("/export")
    public ResponseEntity<byte[]> exportExcel() {
        byte[] data = teacherService.exportToExcel();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=teachers.xlsx");

        return ResponseEntity
                .ok()
                .headers(headers)
                .body(data);
    }
}


