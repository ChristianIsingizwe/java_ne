package com.example.javaexam.dtos.payment;

import com.example.javaexam.models.enums.PaymentMethod;
import com.example.javaexam.models.enums.PaymentStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record PaymentResponse(
        Long id,
        String paymentReference,
        String billReference,
        String customerName,
        BigDecimal amountPaid,
        PaymentMethod paymentMethod,
        LocalDate paymentDate,
        PaymentStatus status,
        LocalDateTime approvedAt
) {
}
