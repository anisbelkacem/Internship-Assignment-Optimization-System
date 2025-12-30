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

    /**
     * Deactivates a course instead of deleting it permanently.
     *
     * Courses are referenced by students and completed internships.
     * Physically deleting a course would break historical data.
     *
     * Using soft-deactivation (active = false) allows us to:
     * - Preserve historical records
     * - Keep existing student and internship data consistent
     * - Prevent the course from being selected in future operations
     */
    @PreAuthorize("hasAnyAuthority('EDIT')")
    @DeleteMapping("/{id}")
    public void deactivateCourse(@PathVariable Long id) {
        courseService.deactivateCourse(id);
    }
}
