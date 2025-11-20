package com.aspd.backend.controller;

import com.aspd.backend.dto.UserCreate;
import com.aspd.backend.dto.UserResponse;
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
    public ResponseEntity<UserResponse> createUser(@RequestBody UserCreate userDto){
        return ResponseEntity.ok(userService.createUser(userDto));
    }

    @PreAuthorize("hasRole('ADMIN') or hasAuthority('MANAGE_USERS')")
    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers(){
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @PreAuthorize("hasRole('ADMIN') or hasAuthority('MANAGE_USERS')")
    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(@PathVariable Long id, @RequestBody UserCreate userDto) {
        return ResponseEntity.ok(userService.updateUser(id, userDto));
    }

    @PreAuthorize("hasRole('ADMIN') or hasAuthority('MANAGE_USERS')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
