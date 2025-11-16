package com.aspd.backend.controller;

import com.aspd.backend.dto.UserCreateDTO;
import com.aspd.backend.dto.UserResponseDTO;
import com.aspd.backend.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
public class UserAdminController {

    private final UserService userService;
    public UserAdminController(UserService userService){ this.userService = userService; }

    @PreAuthorize("hasRole('ADMIN') or hasAuthority('MANAGE_USERS')")
    @PostMapping
    public ResponseEntity<UserResponseDTO> createUser(@RequestBody UserCreateDTO userCreateDTO){
        return ResponseEntity.ok(userService.createUser(userCreateDTO));
    }

    @PreAuthorize("hasRole('ADMIN') or hasAuthority('MANAGE_USERS')")
    @GetMapping
    public ResponseEntity<List<UserResponseDTO>> getAllUsers(){
        return ResponseEntity.ok(userService.getAllUsers());
    }
}
