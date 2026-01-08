package com.aspd.backend.service;

import com.aspd.backend.common.exception.CourseAlreadyExistsException;
import com.aspd.backend.common.exception.CourseNotFoundExeption;
import com.aspd.backend.model.Course;
import com.aspd.backend.repository.CourseRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CourseService {

    private final CourseRepository courseRepository;

    public CourseService(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    public List<Course> getAllCourses() {
        return courseRepository.findAll();
    }

    public List<Course> getActiveCourses() {
        return courseRepository.findByActiveTrue();
    }

    public Course getCourseById(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new CourseNotFoundExeption("Course not found"));
    }

    public Course createCourse(Course course) {
        if (courseRepository.existsByName(course.getName())) {
            throw new CourseAlreadyExistsException("Course already exists");
        }
        return courseRepository.save(course);
    }

    public Course updateCourse(Long id, Course updated) {
        Course course = getCourseById(id);

        course.setName(updated.getName());
        course.setActive(updated.isActive());

        return courseRepository.save(course);
    }

    public Course deactivateCourse(Long id) {
        Course course = getCourseById(id);
        course.setActive(false);
        return courseRepository.save(course);
    }

    public void deleteCourse(Long id) {
        Course course = getCourseById(id);
        courseRepository.delete(course);
    }
}
