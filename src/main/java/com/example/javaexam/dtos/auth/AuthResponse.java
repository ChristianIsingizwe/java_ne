package com.example.javaexam.dtos.auth;

import com.example.javaexam.dtos.user.UserResponse;

public record AuthResponse(
        String accessToken,
        String tokenType,
        long expiresIn,
        UserResponse user
) {
}
