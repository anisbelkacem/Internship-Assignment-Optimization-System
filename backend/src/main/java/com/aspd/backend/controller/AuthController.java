package com.aspd.backend.controller;

import com.aspd.backend.dto.LoginRequest;
import com.aspd.backend.dto.LoginResponse;
import com.aspd.backend.security.JwtUtil;
import com.aspd.backend.service.DbUserDetailsService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final DbUserDetailsService userDetailsService;

    public AuthController(PasswordEncoder passwordEncoder, JwtUtil jwtUtil, DbUserDetailsService userDetailsService) {
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
        List<String> authorities = user.getAuthorities().stream()
                .map(auth -> auth.getAuthority())
                .collect(Collectors.toList());
        claims.put("authorities", authorities);
        String token = jwtUtil.generateToken(request.getEmail(), claims);

        return ResponseEntity.ok(new LoginResponse(token));
    }
}
