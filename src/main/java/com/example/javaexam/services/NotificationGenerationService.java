package com.example.javaexam.services;

import com.example.javaexam.models.Bill;
import com.example.javaexam.models.Notification;
import com.example.javaexam.models.Payment;
import com.example.javaexam.models.enums.BillStatus;
import com.example.javaexam.models.enums.NotificationType;
import com.example.javaexam.repositories.NotificationRepository;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationGenerationService {

    private final NotificationRepository notificationRepository;

    public void createBillNotificationIfMissing(Bill bill) {
        if (notificationRepository.findLatestBillNotification(bill.getId()).isPresent()) {
            return;
        }

        notificationRepository.save(Notification.builder()
                .customer(bill.getCustomer())
                .bill(bill)
                .notificationType(NotificationType.BILL_GENERATED)
                .message("Dear %s.\nYour %s utility bill of %s FRW has been successfully processed."
                        .formatted(
                                bill.getCustomer().getProfile().getFullName(),
                                billPeriod(bill),
                                bill.getTotalAmount()))
                .build());
    }

    public void createPaymentNotificationIfMissing(Payment payment) {
        if (notificationRepository.findLatestPaymentNotification(payment.getId()).isPresent()) {
            return;
        }

        Bill bill = payment.getBill();
        boolean fullyPaid = bill.getStatus() == BillStatus.PAID;
        notificationRepository.save(Notification.builder()
                .customer(bill.getCustomer())
                .bill(bill)
                .payment(payment)
                .notificationType(fullyPaid ? NotificationType.PAYMENT_COMPLETED : NotificationType.PARTIAL_PAYMENT_RECEIVED)
                .message(fullyPaid
                        ? "Dear %s.\nYour %s utility bill of %s FRW has been fully paid."
                                .formatted(
                                        bill.getCustomer().getProfile().getFullName(),
                                        billPeriod(bill),
                                        bill.getTotalAmount())
                        : "Dear %s.\nWe have received your partial payment of %s FRW for the %s utility bill. Remaining balance: %s FRW."
                                .formatted(
                                        bill.getCustomer().getProfile().getFullName(),
                                        payment.getAmountPaid(),
                                        billPeriod(bill),
                                        bill.getOutstandingBalance()))
                .build());
    }

    private String billPeriod(Bill bill) {
        return "%s / %d".formatted(
                YearMonth.of(bill.getBillingYear(), bill.getBillingMonth())
                        .getMonth()
                        .getDisplayName(TextStyle.FULL, Locale.ENGLISH),
                bill.getBillingYear());
    }
}
