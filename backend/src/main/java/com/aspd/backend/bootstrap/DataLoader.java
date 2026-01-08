package com.aspd.backend.bootstrap;

import com.aspd.backend.model.Permission;
import com.aspd.backend.model.User;
import com.aspd.backend.model.UserRole;
import com.aspd.backend.repository.UserRepository;
import com.aspd.backend.repository.SchoolRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class DataLoader implements ApplicationRunner {

    private final UserRepository userRepository;
    private final SchoolRepository schoolRepository;
    private final PasswordEncoder passwordEncoder;

    public DataLoader(UserRepository userRepository, SchoolRepository schoolRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.schoolRepository = schoolRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args)  {
        String adminEmail = "admin@school.com";
        if (userRepository.findByEmail(adminEmail).isEmpty()) {
            User admin = User.builder()
                    .firstName("Super")
                    .lastName("Admin")
                    .email(adminEmail)
                    .password(passwordEncoder.encode("admin123"))
                    .roles(Set.of(UserRole.ADMIN))
                    .permissions(Set.of(Permission.MANAGE_USERS, Permission.VIEW, Permission.EDIT))
                    .build();
            userRepository.save(admin);
        }

        // Ensure legacy schools are marked active
        schoolRepository.findAll().stream()
                .filter(s -> s.getActive() == null)
                .forEach(s -> {
                    s.setActive(true);
                    schoolRepository.save(s);
                });
    }
}
