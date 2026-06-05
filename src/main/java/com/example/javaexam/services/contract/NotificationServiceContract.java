package com.example.javaexam.services.contract;

import com.example.javaexam.dtos.notification.NotificationResponse;
import java.util.List;

public interface NotificationServiceContract {

    List<NotificationResponse> listByCustomer(Long customerId);

    List<NotificationResponse> listForCurrentCustomer(String email);
}
