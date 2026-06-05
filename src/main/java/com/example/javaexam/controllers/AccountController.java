package com.example.javaexam.controllers;

import com.example.javaexam.security.dtos.AuthResponse;
import com.example.javaexam.security.dtos.ChangePasswordRequest;
import com.example.javaexam.models.domains.ApiResponse;
import com.example.javaexam.models.User;
import com.example.javaexam.services.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Authenticated session/account management. Requires a valid access token
 * (enforced by SecurityConfig's {@code anyRequest().authenticated()}).
 */
@RestController
@RequestMapping("/api/account")
@RequiredArgsConstructor
@Tag(name = "Account / Session", description = "Change password and sign out of all devices (JWT required)")
public class AccountController {

    private final AuthService authService;

    @PutMapping("/password")
    @Operation(summary = "Change password; signs out other sessions and returns a fresh token pair")
    public AuthResponse changePassword(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody ChangePasswordRequest request) {
        return authService.changePassword(user.getEmail(), request);
    }

    @PostMapping("/logout-all")
    @Operation(summary = "Revoke every session for the current user")
    public ApiResponse logoutAll(@AuthenticationPrincipal User user) {
        return authService.logoutAll(user.getEmail());
    }
}
