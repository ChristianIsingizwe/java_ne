package com.example.javaexam.services;

import com.example.javaexam.events.BillApprovedEvent;
import com.example.javaexam.events.PaymentApprovedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationEmailEventListener {

    private final NotificationEmailService notificationEmailService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onBillApproved(BillApprovedEvent event) {
        try {
            notificationEmailService.sendBillNotificationIfPending(event.billId());
        } catch (Exception ex) {
            log.error("Failed to process approved bill email notification for bill {}", event.billId(), ex);
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onPaymentApproved(PaymentApprovedEvent event) {
        try {
            notificationEmailService.sendPaymentNotificationIfPending(event.paymentId());
        } catch (Exception ex) {
            log.error("Failed to process payment email notification for payment {}", event.paymentId(), ex);
        }
    }
}
