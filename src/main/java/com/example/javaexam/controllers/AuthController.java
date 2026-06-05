package com.example.javaexam.controllers;

import com.example.javaexam.security.dtos.AuthResponse;
import com.example.javaexam.security.dtos.ForgotPasswordRequest;
import com.example.javaexam.security.dtos.LoginRequest;
import com.example.javaexam.security.dtos.LogoutRequest;
import com.example.javaexam.models.domains.ApiResponse;
import com.example.javaexam.security.dtos.RefreshRequest;
import com.example.javaexam.security.dtos.RegisterRequest;
import com.example.javaexam.security.dtos.ResendVerificationRequest;
import com.example.javaexam.security.dtos.ResetPasswordRequest;
import com.example.javaexam.services.AuthService;
import com.example.javaexam.services.TokenService;
import com.example.javaexam.utils.ValidationPatterns;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Public authentication endpoints (no access token required). */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Validated
@Tag(name = "Authentication", description = "Registration, verification, login, token refresh, and password reset")
@SecurityRequirements // public endpoints: clears the global JWT requirement in Swagger UI
public class AuthController {

    private final AuthService authService;
    private final TokenService tokenService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Register a new user and send a verification email")
    public ApiResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @GetMapping("/verify")
    @Operation(summary = "Confirm an email-verification token and enable the account")
    public ApiResponse verify(
            @RequestParam("token")
            @NotBlank(message = "Token is required")
            @Pattern(regexp = ValidationPatterns.UUID, message = "Token must be a valid UUID")
            @Parameter(example = "5c0f0d7e-2f7d-4f91-9d73-8e9b7d2a66f1")
            String token) {
        return authService.verify(token);
    }

    @PostMapping("/resend-verification")
    @Operation(summary = "Resend the verification email for an unverified account")
    public ApiResponse resendVerification(@Valid @RequestBody ResendVerificationRequest request) {
        return authService.resendVerification(request.email());
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticate and receive an access + refresh token pair")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/refresh")
    @Operation(summary = "Exchange a refresh token for a new token pair (rotates the refresh token)")
    public AuthResponse refresh(@Valid @RequestBody RefreshRequest request) {
        return tokenService.refresh(request.refreshToken());
    }

    @PostMapping("/logout")
    @Operation(summary = "Revoke a refresh token (logs out the current device)")
    public ApiResponse logout(@Valid @RequestBody LogoutRequest request) {
        tokenService.logout(request.refreshToken());
        return new ApiResponse("Logged out.");
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Send a password-reset link to the account's email")
    public ApiResponse forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        return authService.forgotPassword(request.email());
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Set a new password using a reset token (revokes all sessions)")
    public ApiResponse resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        return authService.resetPassword(request.token(), request.newPassword());
    }
}
