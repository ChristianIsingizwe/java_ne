package com.example.javaexam.dtos.notification;

import com.example.javaexam.models.enums.NotificationStatus;
import com.example.javaexam.models.enums.NotificationType;
import java.time.LocalDateTime;

public record NotificationResponse(
        Long id,
        Long customerId,
        String customerName,
        String billReference,
        String paymentReference,
        NotificationType notificationType,
        NotificationStatus status,
        String message,
        LocalDateTime createdAt
) {
}
