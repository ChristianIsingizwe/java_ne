package com.example.javaexam.dtos.user;

import com.example.javaexam.models.enums.RecordStatus;
import java.util.List;

public record UserResponse(
        Long id,
        Long profileId,
        Long customerId,
        String fullName,
        String email,
        String phoneNumber,
        String nationalId,
        String address,
        RecordStatus status,
        List<String> roles
) {
}
