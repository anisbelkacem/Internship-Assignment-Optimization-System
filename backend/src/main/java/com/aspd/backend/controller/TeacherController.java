package com.aspd.backend.controller;

import com.aspd.backend.model.Student;
import com.aspd.backend.model.Teacher;
import com.aspd.backend.service.StudentService;
import com.aspd.backend.service.TeacherService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/teachers")
public class TeacherController {

    private final TeacherService teacherService;
    public TeacherController(TeacherService teacherService){ this.teacherService = teacherService; }

    @PreAuthorize("hasRole('ADMIN') or hasAuthority('MANAGE_STUDENTS')")
    @PostMapping
    public ResponseEntity<Teacher> createTeacher(Teacher teacher){
        return ResponseEntity.ok(teacherService.createTeacher(teacher));
    }

    @PreAuthorize("hasRole('ADMIN') or hasAuthority('MANAGE_TEACHERS')")
    @GetMapping
    public ResponseEntity<List<Teacher>>  getAllTeachers(){
        return ResponseEntity.ok(teacherService.getAllTeachers());
    }
}
