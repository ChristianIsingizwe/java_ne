package com.example.javaexam.services;

import com.example.javaexam.dtos.notification.NotificationResponse;
import com.example.javaexam.exceptions.ApiException;
import com.example.javaexam.mappers.ApplicationMapper;
import com.example.javaexam.models.Customer;
import com.example.javaexam.models.enums.BillStatus;
import com.example.javaexam.models.enums.PaymentStatus;
import com.example.javaexam.repositories.CustomerRepository;
import com.example.javaexam.repositories.NotificationRepository;
import com.example.javaexam.services.contract.NotificationServiceContract;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationService implements NotificationServiceContract {

    private final NotificationRepository notificationRepository;
    private final CustomerRepository customerRepository;
    private final ApplicationMapper applicationMapper;

    public List<NotificationResponse> listByCustomer(Long customerId) {
        return notificationRepository.findByCustomerIdOrderByCreatedAtDesc(customerId).stream()
                .map(applicationMapper::toNotificationResponse)
                .toList();
    }

    public List<NotificationResponse> listForCurrentCustomer(String email) {
        Customer customer = customerRepository.findByProfileEmailIgnoreCase(email)
                .orElseThrow(() -> ApiException.notFound("Customer account not found"));

        // Portal users should only see business events that have cleared approval gates; database
        // triggers create notifications earlier than the portal is allowed to expose them.
        return notificationRepository.findByCustomerIdOrderByCreatedAtDesc(customer.getId()).stream()
                .filter(notification -> notification.getBill() == null
                        || notification.getBill().getStatus() == BillStatus.APPROVED
                        || notification.getBill().getStatus() == BillStatus.PARTIALLY_PAID
                        || notification.getBill().getStatus() == BillStatus.PAID)
                .filter(notification -> notification.getPayment() == null
                        || notification.getPayment().getStatus() == PaymentStatus.APPROVED)
                .map(applicationMapper::toNotificationResponse)
                .toList();
    }
}
