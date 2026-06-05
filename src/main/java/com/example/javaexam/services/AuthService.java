package com.example.javaexam.services;

import com.example.javaexam.security.dtos.AuthResponse;
import com.example.javaexam.security.dtos.ChangePasswordRequest;
import com.example.javaexam.security.dtos.LoginRequest;
import com.example.javaexam.models.domains.ApiResponse;
import com.example.javaexam.security.dtos.RegisterRequest;
import com.example.javaexam.exceptions.ApiException;
import com.example.javaexam.models.AuthToken;
import com.example.javaexam.models.enums.Role;
import com.example.javaexam.models.enums.TokenType;
import com.example.javaexam.models.User;
import com.example.javaexam.repositories.AuthTokenRepository;
import com.example.javaexam.repositories.UserRepository;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Account-centric authentication flows: registration, email verification,
 * login, change password, logout-all, and the forgot/reset password cycle.
 * Token issuance and refresh-token lifecycle live in {@link TokenService}.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final AuthTokenRepository authTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;
    private final EmailService emailService;

    @Value("${app.base-url}")
    private String baseUrl;

    @Value("${app.verification.expiration-minutes}")
    private long verificationExpirationMinutes;

    @Value("${app.password-reset.expiration-minutes}")
    private long passwordResetExpirationMinutes;

    // --- Registration & verification --------------------------------------

    @Transactional
    public ApiResponse register(RegisterRequest request) {
        String email = request.email().trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            throw ApiException.conflict("An account with email '" + email + "' already exists");
        }

        User user = User.builder()
                .firstName(request.firstName().trim())
                .lastName(request.lastName().trim())
                .email(email)
                .password(passwordEncoder.encode(request.password()))
                .role(Role.USER)
                .enabled(false)
                .build();
        userRepository.save(user);

        sendVerificationToken(user);

        log.info("Registered new user {} (pending verification)", email);
        return new ApiResponse(
                "Registration successful. Check your email to verify your account.");
    }

    @Transactional
    public ApiResponse verify(String token) {
        AuthToken verificationToken = authTokenRepository.findByTypeAndToken(TokenType.EMAIL_VERIFICATION, token)
                .orElseThrow(() -> ApiException.badRequest("Invalid verification token"));

        if (verificationToken.isConsumed()) {
            throw ApiException.badRequest("This verification link has already been used");
        }
        if (verificationToken.isExpired()) {
            throw ApiException.badRequest("This verification link has expired. Please request a new one.");
        }

        User user = verificationToken.getUser();
        user.setEnabled(true);
        userRepository.save(user);

        verificationToken.setConsumedAt(LocalDateTime.now());
        authTokenRepository.save(verificationToken);

        log.info("Verified email for user {}", user.getEmail());
        return new ApiResponse("Email verified successfully. You can now log in.");
    }

    @Transactional
    public ApiResponse resendVerification(String rawEmail) {
        String email = rawEmail.trim().toLowerCase();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> ApiException.badRequest("No account found for that email"));

        if (user.isEnabled()) {
            throw ApiException.badRequest("This account is already verified");
        }

        sendVerificationToken(user);
        return new ApiResponse("A new verification email has been sent.");
    }

    // --- Login ------------------------------------------------------------

    /**
     * Authenticates credentials and returns a token pair. Spring Security
     * rejects unverified accounts (the {@code enabled} flag) automatically.
     */
    public AuthResponse login(LoginRequest request) {
        String email = request.email().trim().toLowerCase();

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, request.password()));

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found: " + email));

        return tokenService.issueTokens(user);
    }

    // --- Password management ----------------------------------------------

    /**
     * Changes the password of an authenticated user. Bumps the token version
     * (signing out every other session) and returns a fresh token pair so the
     * current device stays signed in.
     */
    @Transactional
    public AuthResponse changePassword(String email, ChangePasswordRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found: " + email));

        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw ApiException.badRequest("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        user.setTokenVersion(user.getTokenVersion() + 1);
        userRepository.save(user);

        log.info("Password changed for {}", email);
        return tokenService.issueTokens(user);
    }

    /** Revokes every session for the user by bumping the token version. */
    @Transactional
    public ApiResponse logoutAll(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found: " + email));

        user.setTokenVersion(user.getTokenVersion() + 1);
        userRepository.save(user);

        log.info("All sessions revoked for {}", email);
        return new ApiResponse("Signed out of all devices.");
    }

    /**
     * Starts the forgot-password flow. Always reports success to avoid
     * revealing whether an account exists for the given email.
     */
    @Transactional
    public ApiResponse forgotPassword(String rawEmail) {
        String email = rawEmail.trim().toLowerCase();

        userRepository.findByEmail(email).ifPresent(user -> {
            String token = UUID.randomUUID().toString();
            AuthToken resetToken = AuthToken.builder()
                    .type(TokenType.PASSWORD_RESET)
                    .token(token)
                    .user(user)
                    .expiresAt(LocalDateTime.now().plusMinutes(passwordResetExpirationMinutes))
                    .build();
            authTokenRepository.save(resetToken);

            String resetUrl = baseUrl + "/reset-password?token=" + token;
            emailService.sendPasswordResetEmail(user.getEmail(), user.getFirstName(), resetUrl);
        });

        return new ApiResponse(
                "If an account exists for that email, a password-reset link has been sent.");
    }

    /** Completes the forgot-password flow and signs out all existing sessions. */
    @Transactional
    public ApiResponse resetPassword(String token, String newPassword) {
        AuthToken resetToken = authTokenRepository.findByTypeAndToken(TokenType.PASSWORD_RESET, token)
                .orElseThrow(() -> ApiException.badRequest("Invalid password-reset token"));

        if (resetToken.isConsumed()) {
            throw ApiException.badRequest("This reset link has already been used");
        }
        if (resetToken.isExpired()) {
            throw ApiException.badRequest("This reset link has expired. Please request a new one.");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setTokenVersion(user.getTokenVersion() + 1);
        userRepository.save(user);

        resetToken.setConsumedAt(LocalDateTime.now());
        authTokenRepository.save(resetToken);

        log.info("Password reset for {}", user.getEmail());
        return new ApiResponse("Password updated successfully. You can now log in.");
    }

    // --- Helpers ----------------------------------------------------------

    private void sendVerificationToken(User user) {
        String token = UUID.randomUUID().toString();
        AuthToken verificationToken = AuthToken.builder()
                .type(TokenType.EMAIL_VERIFICATION)
                .token(token)
                .user(user)
                .expiresAt(LocalDateTime.now().plusMinutes(verificationExpirationMinutes))
                .build();
        authTokenRepository.save(verificationToken);

        String verificationUrl = baseUrl + "/api/auth/verify?token=" + token;
        emailService.sendVerificationEmail(user.getEmail(), user.getFirstName(), verificationUrl);
    }
}
