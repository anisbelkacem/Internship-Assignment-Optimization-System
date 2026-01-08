package com.aspd.backend.controller;

import com.aspd.backend.model.Course;
import com.aspd.backend.service.CourseService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/courses")

public class CourseController {
    private final CourseService courseService;
    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    @PreAuthorize("hasAnyAuthority('VIEW')")
    @GetMapping
    public List<Course> getAllCourses() {
        return courseService.getAllCourses();
    }

    @PreAuthorize("hasAnyAuthority('VIEW')")
    @GetMapping("/active")
    public List<Course> getActiveCourses() {
        return courseService.getActiveCourses();
    }

    @PreAuthorize("hasAnyAuthority('VIEW')")
    @GetMapping("/{id}")
    public Course getCourseById(@PathVariable Long id) {
        return courseService.getCourseById(id);
    }

    @PreAuthorize("hasAnyAuthority('EDIT')")
    @PostMapping
    public Course createCourse(@RequestBody Course course) {
        return courseService.createCourse(course);
    }

    @PreAuthorize("hasAnyAuthority('EDIT')")
    @PutMapping("/{id}")
    public Course updateCourse(@PathVariable Long id, @RequestBody Course course) {
        return courseService.updateCourse(id, course);
    }

    @PreAuthorize("hasAnyAuthority('EDIT')")
    @PutMapping("/{id}/deactivate")
    public Course deactivateCourse(@PathVariable Long id) {
        return courseService.deactivateCourse(id);
    }

    @PreAuthorize("hasAnyAuthority('EDIT')")
    @DeleteMapping("/{id}")
    public void deleteCourse(@PathVariable Long id) {
        courseService.deleteCourse(id);
    }
}
