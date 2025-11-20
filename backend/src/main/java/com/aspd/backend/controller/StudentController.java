package com.aspd.backend.controller;

import com.aspd.backend.model.Student;
import com.aspd.backend.service.StudentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/students")
public class StudentController {

    private final StudentService studentService;
    public StudentController(StudentService studentService){ this.studentService = studentService; }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<Student> createStudent(Student student){
        return ResponseEntity.ok(studentService.createStudent(student));
    }

    @PreAuthorize("hasRole('ADMIN') or hasAuthority('VIEW') or hasAuthority('EDIT')")
    @GetMapping
    public ResponseEntity<List<Student>>  getAllStudents(){
        return ResponseEntity.ok(studentService.getAllStudents());
    }
}
