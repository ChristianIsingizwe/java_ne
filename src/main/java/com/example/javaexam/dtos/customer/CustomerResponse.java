package com.example.javaexam.dtos.customer;

import com.example.javaexam.models.enums.RecordStatus;

public record CustomerResponse(
        Long id,
        Long profileId,
        String fullName,
        String nationalId,
        String email,
        String phoneNumber,
        String address,
        RecordStatus status
) {
}
