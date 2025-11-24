package com.aspd.backend.controller;

import com.aspd.backend.dto.StudentDto;
import com.aspd.backend.model.Student;
import com.aspd.backend.service.StudentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/students")
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @PreAuthorize("hasAuthority('VIEW') or hasAnyAuthority('EDIT')")
    @GetMapping
    public ResponseEntity<List<StudentDto>> getAllStudents() {
        List<StudentDto> dtos = studentService.getAllStudents().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @PreAuthorize("hasAuthority('VIEW') or hasAnyAuthority('EDIT')")
    @GetMapping("/{id}")
    public ResponseEntity<StudentDto> getStudentById(@PathVariable int id) {
        return studentService.getStudentById(id)
                .map(this::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasAnyAuthority('EDIT')")
    @PostMapping
    public ResponseEntity<StudentDto> createStudent(@RequestBody StudentDto dto) {
        Student created = studentService.createStudent(dto);
        return ResponseEntity.ok(toDto(created));
    }

    @PreAuthorize("hasAnyAuthority('EDIT')")
    @PutMapping("/{id}")
    public ResponseEntity<StudentDto> updateStudent(@PathVariable int id, @RequestBody StudentDto dto) {
        Student updated = studentService.updateStudent(id, dto);
        return ResponseEntity.ok(toDto(updated));
    }

    @PreAuthorize("hasAnyAuthority('EDIT')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStudent(@PathVariable int id) {
        studentService.deleteStudent(id);
        return ResponseEntity.noContent().build();
    }

    // Simple mapper method
    private StudentDto toDto(Student student) {
        return new StudentDto(
                student.getMatriculationNbr(),
                student.getFirstName(),
                student.getLastName(),
                student.getEmail(),
                student.getSchoolType(),
                student.getMainCourse(),
                student.getPrefCourse1(),
                student.getPrefCourse2(),
                student.getPrefCourse3(),
                student.isRegistred(),
                student.isOriented(),
                student.getAddress(),
                student.getAddressSemester(),
                student.getPhone(),
                student.getBirthDate(),
                student.getDescription()
        );
    }
}
