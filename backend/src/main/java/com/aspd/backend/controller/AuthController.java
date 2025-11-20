package com.aspd.backend.controller;

import com.aspd.backend.dto.LoginRequest;
import com.aspd.backend.dto.LoginResponse;
import com.aspd.backend.security.JwtUtil;
import com.aspd.backend.service.DbUserDetailsService;
import com.aspd.backend.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final DbUserDetailsService userDetailsService;

    public AuthController(UserService userService, PasswordEncoder passwordEncoder, JwtUtil jwtUtil, DbUserDetailsService userDetailsService){
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        UserDetails user = userDetailsService.loadUserByUsername(request.getEmail());
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return ResponseEntity.status(401).build();
        }
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", user.getAuthorities());
        String token = jwtUtil.generateToken(request.getEmail(), claims);

        return ResponseEntity.ok(new LoginResponse(token));    }
}
