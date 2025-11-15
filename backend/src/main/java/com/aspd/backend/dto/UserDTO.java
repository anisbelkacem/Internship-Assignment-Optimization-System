package com.aspd.backend.dto;

import com.aspd.backend.model.UserRole;
import lombok.Data;

@Data
public class UserDTO {
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private String address;
    private String school;
    private UserRole role;

    // Student specific
    private String mainSubject;

    // Teacher specific
    private String department;
    private Integer yearsOfExperience;
}