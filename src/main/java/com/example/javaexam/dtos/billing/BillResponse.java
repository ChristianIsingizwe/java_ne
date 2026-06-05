package com.example.javaexam.dtos.billing;

import com.example.javaexam.models.enums.BillStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record BillResponse(
        Long id,
        String billReference,
        Long customerId,
        String customerName,
        Long meterId,
        String meterNumber,
        Integer billingYear,
        Integer billingMonth,
        LocalDate dueDate,
        BigDecimal consumptionUnits,
        BigDecimal subtotal,
        BigDecimal vatAmount,
        BigDecimal penaltyAmount,
        BigDecimal totalAmount,
        BigDecimal amountPaid,
        BigDecimal outstandingBalance,
        BillStatus status,
        LocalDateTime approvedAt,
        List<BillLineItemResponse> lineItems
) {
}
