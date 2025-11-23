package com.aspd.backend.controller;

import com.aspd.backend.dto.CompletedInternshipsDto;
import com.aspd.backend.model.CompletedInternships;
import com.aspd.backend.service.CompletedInternshipsService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/completed-internships")
public class CompletedInternshipsController {

    private final CompletedInternshipsService completedInternshipsService;

    public CompletedInternshipsController(CompletedInternshipsService completedInternshipsService) {
        this.completedInternshipsService = completedInternshipsService;
    }

    @PreAuthorize("hasAnyAuthority('EDIT')")
    @PostMapping
    public ResponseEntity<CompletedInternshipsDto> create(@RequestBody CompletedInternshipsDto dto) {
        CompletedInternships created = completedInternshipsService.createCompletedInternship(dto);
        return ResponseEntity.ok(toDto(created));
    }

    @PreAuthorize("hasAuthority('VIEW') or hasAnyAuthority('EDIT')")
    @GetMapping("/{id}")
    public ResponseEntity<CompletedInternshipsDto> getById(@PathVariable Long id) {
        return completedInternshipsService.getCompletedInternshipById(id)
                .map(internship -> ResponseEntity.ok(toDto(internship)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasAuthority('VIEW') or hasAnyAuthority('EDIT')")
    @GetMapping
    public ResponseEntity<List<CompletedInternshipsDto>> getAll() {
        List<CompletedInternshipsDto> dtos =
                completedInternshipsService.getAllCompletedInternships()
                        .stream()
                        .map(this::toDto)
                        .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @PreAuthorize("hasAuthority('VIEW') or hasAnyAuthority('EDIT')")
    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<CompletedInternshipsDto>> getByStudentId(@PathVariable int studentId) {
        List<CompletedInternshipsDto> dtos =
                completedInternshipsService.getInternshipsByStudentId(studentId)
                        .stream()
                        .map(this::toDto)
                        .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @PreAuthorize("hasAuthority('VIEW') or hasAnyAuthority('EDIT')")
    @GetMapping("/teacher/{teacherId}")
    public ResponseEntity<List<CompletedInternshipsDto>> getByTeacherId(@PathVariable Long teacherId) {
        List<CompletedInternshipsDto> dtos =
                completedInternshipsService.getInternshipsByTeacherId(teacherId)
                        .stream()
                        .map(this::toDto)
                        .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @PreAuthorize("hasAuthority('VIEW') or hasAnyAuthority('EDIT')")
    @GetMapping("/school/{schoolId}")
    public ResponseEntity<List<CompletedInternshipsDto>> getBySchoolId(@PathVariable Long schoolId) {
        List<CompletedInternshipsDto> dtos =
                completedInternshipsService.getInternshipsBySchoolId(schoolId)
                        .stream()
                        .map(this::toDto)
                        .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @PreAuthorize("hasAnyAuthority('EDIT')")
    @PutMapping("/{id}")
    public ResponseEntity<CompletedInternshipsDto> update(
            @PathVariable Long id,
            @RequestBody CompletedInternshipsDto dto) {

        CompletedInternships updated = completedInternshipsService.updateCompletedInternship(id, dto);
        return ResponseEntity.ok(toDto(updated));
    }

    @PreAuthorize("hasAnyAuthority('EDIT')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        completedInternshipsService.deleteCompletedInternship(id);
        return ResponseEntity.noContent().build();
    }

    // ----------- Mapping method -----------
    private CompletedInternshipsDto toDto(CompletedInternships ci) {
        return new CompletedInternshipsDto(
                ci.getId(),
                ci.getStudent().getMatriculationNbr(),
                ci.getTeacher().getTeacherId(),
                ci.getSchool().getId(),
                ci.getCourse(),
                ci.getStartDate(),
                ci.getEndDate(),
                ci.getDescription()
        );
    }
}
