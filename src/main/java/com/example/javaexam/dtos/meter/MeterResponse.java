package com.example.javaexam.dtos.meter;

import com.example.javaexam.models.enums.MeterBillingMode;
import com.example.javaexam.models.enums.MeterType;
import com.example.javaexam.models.enums.RecordStatus;
import java.time.LocalDate;

public record MeterResponse(
        Long id,
        Long customerId,
        String customerName,
        String meterNumber,
        MeterType meterType,
        MeterBillingMode billingMode,
        LocalDate installationDate,
        RecordStatus status
) {
}
