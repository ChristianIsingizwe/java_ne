package com.example.javaexam.services;

import com.example.javaexam.dtos.common.StatusUpdateRequest;
import com.example.javaexam.dtos.user.CreateUserRequest;
import com.example.javaexam.dtos.user.UserResponse;
import com.example.javaexam.exceptions.ApiException;
import com.example.javaexam.mappers.ApplicationMapper;
import com.example.javaexam.models.Customer;
import com.example.javaexam.models.Profile;
import com.example.javaexam.models.Role;
import com.example.javaexam.models.User;
import com.example.javaexam.models.enums.RoleName;
import com.example.javaexam.repositories.CustomerRepository;
import com.example.javaexam.repositories.ProfileRepository;
import com.example.javaexam.repositories.RoleRepository;
import com.example.javaexam.repositories.UserRepository;
import com.example.javaexam.utils.InputSanitizer;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final ProfileRepository profileRepository;
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationMapper applicationMapper;

    @Transactional
    public UserResponse create(CreateUserRequest request) {
        String fullName = InputSanitizer.normalizeRequired(request.fullName(), "Full name");
        String email = InputSanitizer.normalizeEmail(request.email());
        String phone = InputSanitizer.normalizeRequired(request.phoneNumber(), "Phone number");
        String nationalId = InputSanitizer.normalizeOptional(request.nationalId());
        String address = InputSanitizer.normalizeOptional(request.address());

        ensureUniqueness(email, nationalId, null);
        if (request.roles().size() != 1) {
            throw ApiException.badRequest("A staff user must have exactly one role");
        }
        RoleName requestedRole = request.roles().iterator().next();
        if (requestedRole != RoleName.ROLE_OPERATOR && requestedRole != RoleName.ROLE_FINANCE) {
            throw ApiException.badRequest("The admin can only create operator or finance users");
        }
        Set<Role> roles = request.roles().stream()
                .map(roleName -> roleRepository.findByName(roleName)
                        .orElseThrow(() -> ApiException.badRequest("Role not found: " + roleName)))
                .collect(java.util.stream.Collectors.toSet());

        Profile profile = Profile.builder()
                .fullName(fullName)
                .email(email)
                .phoneNumber(phone)
                .nationalId(nationalId)
                .address(address)
                .status(request.status())
                .build();

        User user = userRepository.save(User.builder()
                .profile(profile)
                .password(passwordEncoder.encode(request.password()))
                .roles(roles)
                .build());

        return applicationMapper.toUserResponse(user, null);
    }

    public List<UserResponse> list() {
        return userRepository.findAllByOrderByIdAsc().stream()
                .map(user -> applicationMapper.toUserResponse(
                        user,
                        customerRepository.findByProfileId(user.getProfile().getId()).map(Customer::getId).orElse(null)))
                .toList();
    }

    public UserResponse me(String email) {
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> ApiException.notFound("User not found"));
        return applicationMapper.toUserResponse(
                user,
                customerRepository.findByProfileId(user.getProfile().getId()).map(Customer::getId).orElse(null));
    }

    @Transactional
    public UserResponse updateStatus(Long userId, StatusUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> ApiException.notFound("User not found"));
        user.getProfile().setStatus(request.status());
        userRepository.save(user);
        return applicationMapper.toUserResponse(
                user,
                customerRepository.findByProfileId(user.getProfile().getId()).map(Customer::getId).orElse(null));
    }

    private void ensureUniqueness(String email, String nationalId, Long profileId) {
        profileRepository.findByEmailIgnoreCase(email)
                .filter(profile -> !profile.getId().equals(profileId))
                .ifPresent(profile -> {
                    throw ApiException.conflict("Email already exists");
                });
        if (nationalId != null) {
            profileRepository.findByNationalId(nationalId)
                    .filter(profile -> !profile.getId().equals(profileId))
                    .ifPresent(profile -> {
                        throw ApiException.conflict("National ID already exists");
                    });
        }
    }
}
