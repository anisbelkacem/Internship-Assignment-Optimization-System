package com.aspd.backend.controller;

import com.aspd.backend.dto.UserCreate;
import com.aspd.backend.dto.UserResponse;
import com.aspd.backend.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
public class UserAdminController {

    private final UserService userService;
    public UserAdminController(UserService userService){ this.userService = userService; }

    @PreAuthorize("hasAuthority('MANAGE_USERS')")
    @PostMapping
    public ResponseEntity<UserResponse> createUser(@RequestBody UserCreate userDto){
        return ResponseEntity.ok(userService.createUser(userDto));
    }

    @PreAuthorize("hasAuthority('MANAGE_USERS')")
    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers(){
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @PreAuthorize("hasAuthority('MANAGE_USERS')")
    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(@PathVariable Long id, @RequestBody UserCreate userDto) {
        return ResponseEntity.ok(userService.updateUser(id, userDto));
    }

    @PreAuthorize("hasAuthority('MANAGE_USERS')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
