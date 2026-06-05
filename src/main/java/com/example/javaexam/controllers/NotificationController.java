package com.example.javaexam.controllers;

import com.example.javaexam.config.SecuredApiErrorResponses;
import com.example.javaexam.dtos.notification.NotificationResponse;
import com.example.javaexam.services.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications")
@SecuredApiErrorResponses
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/customer/{customerId}")
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE')")
    @Operation(summary = "List notifications for a customer")
    public List<NotificationResponse> byCustomer(@PathVariable Long customerId) {
        return notificationService.listByCustomer(customerId);
    }
}
