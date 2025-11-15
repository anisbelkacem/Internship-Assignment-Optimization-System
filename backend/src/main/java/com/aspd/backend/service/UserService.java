package com.aspd.backend.service;

import com.aspd.backend.dto.UserDTO;
import com.aspd.backend.model.Student;
import com.aspd.backend.model.Teacher;
import com.aspd.backend.model.User;
import com.aspd.backend.model.UserRole;
import com.aspd.backend.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public User createUser(UserDTO userDTO) {
        User user;

        // Create the correct subclass
        if (userDTO.getRole() == UserRole.STUDENT) {
            Student student = new Student();
            student.setMainSubject(userDTO.getMainSubject());
            user = student;
        } else if (userDTO.getRole() == UserRole.TEACHER) {
            Teacher teacher = new Teacher();
            teacher.setDepartment(userDTO.getDepartment());
            teacher.setYearsOfExperience(userDTO.getYearsOfExperience() != null ? userDTO.getYearsOfExperience() : 0);
            user = teacher;
        } else {
            // Admin or generic user
            user = new User();
        }

        // Set common fields
        user.setFirstName(userDTO.getFirstName());
        user.setLastName(userDTO.getLastName());
        user.setEmail(userDTO.getEmail());
        user.setAddress(userDTO.getAddress());
        user.setSchool(userDTO.getSchool());
        user.setRole(userDTO.getRole());
        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));

        // Save: Hibernate automatically handles the subclass table
        return userRepository.save(user);
    }

    public User updateUser(Long id, UserDTO userDTO) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        existingUser.setFirstName(userDTO.getFirstName());
        existingUser.setLastName(userDTO.getLastName());
        existingUser.setEmail(userDTO.getEmail());
        existingUser.setAddress(userDTO.getAddress());
        existingUser.setSchool(userDTO.getSchool());

        if (existingUser instanceof Student student && userDTO.getRole() == UserRole.STUDENT) {
            student.setMainSubject(userDTO.getMainSubject());
        } else if (existingUser instanceof Teacher teacher && userDTO.getRole() == UserRole.TEACHER) {
            teacher.setDepartment(userDTO.getDepartment());
            teacher.setYearsOfExperience(userDTO.getYearsOfExperience() != null ? userDTO.getYearsOfExperience() : teacher.getYearsOfExperience());
        }

        return userRepository.save(existingUser);
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
}