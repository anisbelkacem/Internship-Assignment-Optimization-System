package com.aspd.backend.controller;

import com.aspd.backend.common.exception.InvalidDataException;
import com.aspd.backend.dto.SchoolImportResult;
import com.aspd.backend.dto.SchoolRequest;
import com.aspd.backend.dto.SchoolResponse;
import com.aspd.backend.model.School;
import com.aspd.backend.service.SchoolService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/schools")
public class SchoolController {
    private final SchoolService service;

    public SchoolController(SchoolService service) {
        this.service = service;
    }

    @PreAuthorize("hasAuthority('VIEW') or hasAnyAuthority('EDIT')")
    @GetMapping
    public List<SchoolResponse> list() {
        return service.list().stream().map(this::toResponse).collect(Collectors.toList());
    }

    @PreAuthorize("hasAuthority('VIEW') or hasAnyAuthority('EDIT')")
    @GetMapping("/{id}")
    public SchoolResponse get(@PathVariable Long id) {
        return toResponse(service.get(id));
    }

    @PreAuthorize("hasAnyAuthority('EDIT')")
    @PostMapping
    public SchoolResponse create(@Valid @RequestBody SchoolRequest req) {
        return toResponse(service.create(req));
    }

    @PreAuthorize("hasAnyAuthority('EDIT')")
    @PutMapping("/{id}")
    public SchoolResponse update(@PathVariable Long id, @Valid @RequestBody SchoolRequest req) {
        return toResponse(service.update(id, req));
    }

    @PreAuthorize("hasAnyAuthority('EDIT')")
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    @PreAuthorize("hasAnyAuthority('EDIT')")
    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public SchoolImportResult importExcel(@RequestPart("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new InvalidDataException("file", null, "is required");
        }
        if (!file.getOriginalFilename().toLowerCase().endsWith(".xlsx")) {
            throw new InvalidDataException("file", file.getOriginalFilename(), "Only .xlsx files are supported");
        }
        return service.importFromExcel(file.getInputStream());
    }

    private SchoolResponse toResponse(School s) {
        SchoolResponse r = new SchoolResponse();
        r.setId(s.getId());
        r.setName(s.getName());
        r.setAddress(s.getAddress());
        r.setZone(s.getZone());
        r.setOepnv(s.getOepnv());
        r.setType(s.getType());
        return r;
    }
}
