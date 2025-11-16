package com.aspd.backend.service;

import com.aspd.backend.model.Student;
import com.aspd.backend.repository.StudentRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StudentService {
    private final StudentRepository studentRepository;

    public StudentService(StudentRepository repo)
    { this.studentRepository = repo;
    }
    public Student createStudent(Student s){
        return studentRepository.save(s);
    }
    public List<Student> getAllStudents(){
        return studentRepository.findAll();
    }
}
