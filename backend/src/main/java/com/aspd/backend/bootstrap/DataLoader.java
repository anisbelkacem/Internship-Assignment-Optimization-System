package com.aspd.backend.bootstrap;

import com.aspd.backend.model.Permission;
import com.aspd.backend.model.User;
import com.aspd.backend.model.UserRole;
import com.aspd.backend.repository.UserRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class DataLoader implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataLoader(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        String adminEmail = "admin@school.com";
        if (userRepository.findByEmail(adminEmail).isEmpty()) {
            User admin = User.builder()
                    .firstName("Super")
                    .lastName("Admin")
                    .email(adminEmail)
                    .password(passwordEncoder.encode("admin123"))
                    .roles(Set.of(UserRole.ADMIN))
                    .permissions(Set.of(Permission.MANAGE_USERS, Permission.MANAGE_STUDENTS, Permission.MANAGE_TEACHERS))
                    .build();
            userRepository.save(admin);
            System.out.println("Seeded admin user: " + adminEmail);
        }
    }
}
