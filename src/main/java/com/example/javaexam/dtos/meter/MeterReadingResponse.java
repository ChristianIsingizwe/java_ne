package com.example.javaexam.dtos.meter;

import java.math.BigDecimal;
import java.time.LocalDate;

public record MeterReadingResponse(
        Long id,
        Long meterId,
        String meterNumber,
        String customerName,
        BigDecimal previousReading,
        BigDecimal currentReading,
        BigDecimal consumption,
        LocalDate readingDate,
        Integer readingYear,
        Integer readingMonth
) {
}
