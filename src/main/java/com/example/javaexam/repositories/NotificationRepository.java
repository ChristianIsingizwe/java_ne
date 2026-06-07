package com.example.javaexam.repositories;

import com.example.javaexam.models.Notification;
import com.example.javaexam.models.enums.NotificationType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    @EntityGraph(attributePaths = {"customer", "customer.profile", "bill", "payment"})
    List<Notification> findByCustomerIdOrderByCreatedAtDesc(Long customerId);

    @EntityGraph(attributePaths = {"customer", "customer.profile", "bill", "payment"})
    Optional<Notification> findTopByBillIdAndNotificationTypeOrderByCreatedAtDesc(Long billId, NotificationType notificationType);

    @EntityGraph(attributePaths = {"customer", "customer.profile", "bill", "payment"})
    Optional<Notification> findTopByPaymentIdAndNotificationTypeOrderByCreatedAtDesc(Long paymentId, NotificationType notificationType);

    @EntityGraph(attributePaths = {"customer", "customer.profile", "bill", "payment"})
    Optional<Notification> findTopByPaymentIdOrderByCreatedAtDesc(Long paymentId);

    default Optional<Notification> findLatestBillNotification(Long billId) {
        return findTopByBillIdAndNotificationTypeOrderByCreatedAtDesc(billId, NotificationType.BILL_GENERATED);
    }

    default Optional<Notification> findLatestPaymentNotification(Long paymentId) {
        return findTopByPaymentIdOrderByCreatedAtDesc(paymentId);
    }
}
