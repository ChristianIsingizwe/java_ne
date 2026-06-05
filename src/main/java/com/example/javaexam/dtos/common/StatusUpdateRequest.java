package com.example.javaexam.dtos.common;

import com.example.javaexam.models.enums.RecordStatus;
import jakarta.validation.constraints.NotNull;

public record StatusUpdateRequest(
        @NotNull RecordStatus status
) {
}
