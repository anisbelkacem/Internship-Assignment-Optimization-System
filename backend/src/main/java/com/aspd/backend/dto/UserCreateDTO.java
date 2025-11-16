package com.aspd.backend.dto;

import com.aspd.backend.model.Permission;
import com.aspd.backend.model.UserRole;
import lombok.Data;

import java.util.Set;

@Data
public class UserCreateDTO {
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private String address;
    private String school;
    private Set<UserRole> roles;
    private Set<Permission> permissions;
}