package com.example.javaexam.services;

import com.example.javaexam.config.NotificationEmailProperties;
import com.example.javaexam.models.Notification;
import com.example.javaexam.models.enums.NotificationStatus;
import com.example.javaexam.repositories.NotificationRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationEmailService {

    private final NotificationRepository notificationRepository;
    private final ResendEmailClient resendEmailClient;
    private final NotificationEmailProperties properties;

    @Transactional
    public void sendBillNotificationIfPending(Long billId) {
        notificationRepository.findLatestBillNotification(billId)
                .ifPresent(this::sendIfPending);
    }

    @Transactional
    public void sendPaymentNotificationIfPending(Long paymentId) {
        notificationRepository.findLatestPaymentNotification(paymentId)
                .ifPresent(this::sendIfPending);
    }

    private void sendIfPending(Notification notification) {
        if (notification.getStatus() == NotificationStatus.SENT) {
            return;
        }
        if (!properties.isEnabled()) {
            log.info("Notification email delivery is disabled; leaving notification {} as {}", notification.getId(),
                    notification.getStatus());
            return;
        }

        Optional<String> recipient = Optional.ofNullable(notification.getCustomer())
                .map(customer -> customer.getProfile())
                .map(profile -> profile.getEmail());
        if (recipient.isEmpty()) {
            log.warn("Skipping email notification {} because recipient email is missing", notification.getId());
            return;
        }

        resendEmailClient.send(
                recipient.get(),
                buildSubject(notification),
                toHtml(notification.getMessage()),
                notification.getMessage());
        notification.setStatus(NotificationStatus.SENT);
        notificationRepository.save(notification);
    }

    private String buildSubject(Notification notification) {
        return switch (notification.getNotificationType()) {
            case BILL_GENERATED -> "Your utility bill is ready";
            case PARTIAL_PAYMENT_RECEIVED -> "We received your partial utility bill payment";
            case PAYMENT_COMPLETED -> "Your utility bill has been fully paid";
        };
    }

    private String toHtml(String plainTextMessage) {
        String escaped = plainTextMessage
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;")
                .replace("\n", "<br/>");
        return "<p>" + escaped + "</p>";
    }
}
