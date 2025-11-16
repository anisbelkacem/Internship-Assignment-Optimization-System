package com.aspd.backend.service;

import com.aspd.backend.common.exception.EmailAlreadyUsedException;
import com.aspd.backend.dto.UserCreateDTO;
import com.aspd.backend.dto.UserResponseDTO;
import com.aspd.backend.model.Student;
import com.aspd.backend.model.Teacher;
import com.aspd.backend.model.User;
import com.aspd.backend.model.UserRole;
import com.aspd.backend.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<UserResponseDTO> getAllUsers() {
        return userRepository.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public UserResponseDTO createUser(UserCreateDTO userDTO) {
        if (userRepository.findByEmail(userDTO.getEmail()).isPresent()) {
            throw new EmailAlreadyUsedException("Email already exists");
        }
        User user = User.builder()
                .firstName(userDTO.getFirstName())
                .lastName(userDTO.getLastName())
                .email(userDTO.getEmail())
                .password(passwordEncoder.encode(userDTO.getPassword()))
                .address(userDTO.getAddress())
                .school(userDTO.getSchool())
                .roles(userDTO.getRoles() == null ? Set.of() : userDTO.getRoles())
                .permissions(userDTO.getPermissions() == null ? Set.of() : userDTO.getPermissions())
                .build();

        User savedUser = userRepository.save(user);
        return toDto(savedUser);
    }

    private UserResponseDTO toDto(User user) {
        UserResponseDTO userResponseDTO = new UserResponseDTO();
        userResponseDTO.setId(user.getId());
        userResponseDTO.setFirstName(user.getFirstName());
        userResponseDTO.setLastName(user.getLastName());
        userResponseDTO.setEmail(user.getEmail());
        userResponseDTO.setAddress(user.getAddress());
        userResponseDTO.setSchool(user.getSchool());
        userResponseDTO.setRoles(user.getRoles());
        userResponseDTO.setPermissions(user.getPermissions());
        return userResponseDTO;
    }
}