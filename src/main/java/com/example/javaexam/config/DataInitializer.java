package com.example.javaexam.config;

import com.example.javaexam.models.enums.Role;
import com.example.javaexam.models.User;
import com.example.javaexam.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Seeds a verified {@code ADMIN} account on startup (if it does not already
 * exist) so the role-based endpoints can be exercised out of the box.
 * Credentials are configurable via {@code app.admin.*} properties.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${app.admin.password}")
    private String adminPassword;

    @Value("${app.admin.first-name}")
    private String adminFirstName;

    @Value("${app.admin.last-name}")
    private String adminLastName;

    @Override
    public void run(String... args) {
        String email = adminEmail.trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            return;
        }

        User admin = User.builder()
                .firstName(adminFirstName)
                .lastName(adminLastName)
                .email(email)
                .password(passwordEncoder.encode(adminPassword))
                .role(Role.ADMIN)
                .enabled(true)
                .build();
        userRepository.save(admin);

        log.info("Seeded ADMIN account: {}", email);
    }
}
