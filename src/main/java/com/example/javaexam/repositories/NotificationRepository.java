package com.example.javaexam.repositories;

import com.example.javaexam.models.Notification;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    @EntityGraph(attributePaths = {"customer", "customer.profile", "bill", "payment"})
    List<Notification> findByCustomerIdOrderByCreatedAtDesc(Long customerId);
}
