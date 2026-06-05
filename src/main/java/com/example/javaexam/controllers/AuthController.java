package com.example.javaexam.controllers;

import com.example.javaexam.config.PublicApiErrorResponses;
import com.example.javaexam.config.SecuredApiErrorResponses;
import com.example.javaexam.dtos.auth.AuthResponse;
import com.example.javaexam.dtos.auth.LoginRequest;
import com.example.javaexam.dtos.auth.SignupRequest;
import com.example.javaexam.services.contract.AuthServiceContract;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication")
@PublicApiErrorResponses
public class AuthController {

    private final AuthServiceContract authService;

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Sign up a new customer portal user")
    @SecurityRequirements
    public AuthResponse signup(@Valid @RequestBody SignupRequest request) {
        return authService.signup(request);
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticate a user and return a JWT")
    @SecurityRequirements
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Log out by revoking the current access token")
    @SecuredApiErrorResponses
    public void logout(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader) {
        authService.logout(authorizationHeader);
    }
}
