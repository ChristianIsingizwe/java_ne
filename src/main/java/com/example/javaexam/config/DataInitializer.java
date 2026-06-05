package com.example.javaexam.config;

import com.example.javaexam.models.Profile;
import com.example.javaexam.models.Role;
import com.example.javaexam.models.User;
import com.example.javaexam.models.enums.RecordStatus;
import com.example.javaexam.models.enums.RoleName;
import com.example.javaexam.repositories.RoleRepository;
import com.example.javaexam.repositories.UserRepository;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${app.admin.password}")
    private String adminPassword;

    @Value("${app.admin.full-name}")
    private String adminFullName;

    @Value("${app.admin.phone}")
    private String adminPhone;

    @Override
    public void run(String... args) {
        for (RoleName roleName : RoleName.values()) {
            roleRepository.findByName(roleName)
                    .orElseGet(() -> roleRepository.save(Role.builder().name(roleName).build()));
        }

        String email = adminEmail.trim().toLowerCase();
        if (userRepository.existsByEmailIgnoreCase(email)) {
            return;
        }

        Role adminRole = roleRepository.findByName(RoleName.ROLE_ADMIN)
                .orElseThrow(() -> new IllegalStateException("ROLE_ADMIN was not seeded"));

        User admin = User.builder()
                .profile(Profile.builder()
                        .fullName(adminFullName)
                        .email(email)
                        .phoneNumber(adminPhone)
                        .status(RecordStatus.ACTIVE)
                        .build())
                .password(passwordEncoder.encode(adminPassword))
                .roles(Set.of(adminRole))
                .build();
        userRepository.save(admin);

        log.info("Seeded ADMIN account: {}", email);
    }
}
