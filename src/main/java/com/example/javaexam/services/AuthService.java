package com.example.javaexam.services;

import com.example.javaexam.dtos.auth.AuthResponse;
import com.example.javaexam.dtos.auth.LoginRequest;
import com.example.javaexam.dtos.auth.SignupRequest;
import com.example.javaexam.exceptions.ApiException;
import com.example.javaexam.mappers.ApplicationMapper;
import com.example.javaexam.models.Customer;
import com.example.javaexam.models.Profile;
import com.example.javaexam.models.Role;
import com.example.javaexam.models.User;
import com.example.javaexam.models.enums.RecordStatus;
import com.example.javaexam.models.enums.RoleName;
import com.example.javaexam.repositories.CustomerRepository;
import com.example.javaexam.repositories.ProfileRepository;
import com.example.javaexam.repositories.RoleRepository;
import com.example.javaexam.repositories.UserRepository;
import com.example.javaexam.security.JwtService;
import com.example.javaexam.services.contract.AuthServiceContract;
import com.example.javaexam.utils.InputSanitizer;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService implements AuthServiceContract {

    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final CustomerRepository customerRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final ApplicationMapper applicationMapper;

    @Transactional
    public AuthResponse signup(SignupRequest request) {
        String fullName = InputSanitizer.normalizeRequired(request.fullName(), "Full name");
        String email = InputSanitizer.normalizeEmail(request.email());
        String phone = InputSanitizer.normalizeRequired(request.phoneNumber(), "Phone number");
        String nationalId = InputSanitizer.normalizeRequired(request.nationalId(), "National ID");
        String address = InputSanitizer.normalizeRequired(request.address(), "Address");

        ensureProfileUniqueness(email, phone, nationalId, null);

        Role customerRole = roleRepository.findByName(RoleName.ROLE_CUSTOMER)
                .orElseThrow(() -> new IllegalStateException("ROLE_CUSTOMER is missing"));

        Profile profile = Profile.builder()
                .fullName(fullName)
                .email(email)
                .phoneNumber(phone)
                .nationalId(nationalId)
                .address(address)
                .status(RecordStatus.ACTIVE)
                .build();

        // Customer portal users participate in both auth and billing flows, so signup creates
        // the domain customer record alongside the login-capable user record.
        Customer customer = customerRepository.save(Customer.builder().profile(profile).build());
        User user = userRepository.save(User.builder()
                .profile(profile)
                .password(passwordEncoder.encode(request.password()))
                .roles(java.util.Set.of(customerRole))
                .build());

        return buildAuthResponse(user, customer.getId());
    }

    public AuthResponse login(LoginRequest request) {
        String email = InputSanitizer.normalizeEmail(request.email());
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, request.password()));

        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found"));
        Long customerId = customerRepository.findByProfileId(user.getProfile().getId())
                .map(Customer::getId)
                .orElse(null);
        return buildAuthResponse(user, customerId);
    }

    private AuthResponse buildAuthResponse(User user, Long customerId) {
        return new AuthResponse(
                jwtService.generateToken(user),
                "Bearer",
                jwtService.getAccessExpirationMs(),
                applicationMapper.toUserResponse(user, customerId));
    }

    private void ensureProfileUniqueness(String email, String phone, String nationalId, Long profileId) {
        profileRepository.findByEmailIgnoreCase(email)
                .filter(profile -> !profile.getId().equals(profileId))
                .ifPresent(profile -> {
                    throw ApiException.conflict("Email already exists");
                });
        profileRepository.findByPhoneNumber(phone)
                .filter(profile -> !profile.getId().equals(profileId))
                .ifPresent(profile -> {
                    throw ApiException.conflict("Phone number already exists");
                });
        profileRepository.findByNationalId(nationalId)
                .filter(profile -> !profile.getId().equals(profileId))
                .ifPresent(profile -> {
                    throw ApiException.conflict("National ID already exists");
                });
    }
}
