package com.example.javaexam.dtos.tariff;

import com.example.javaexam.models.enums.MeterType;
import com.example.javaexam.models.enums.TariffType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record TariffVersionResponse(
        Long id,
        MeterType meterType,
        TariffType tariffType,
        Integer versionNumber,
        LocalDate effectiveFrom,
        BigDecimal fixedServiceCharge,
        BigDecimal vatRate,
        BigDecimal latePaymentPenaltyRate,
        List<TariffTierResponse> tiers
) {
}
