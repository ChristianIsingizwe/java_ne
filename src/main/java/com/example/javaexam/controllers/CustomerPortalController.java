package com.example.javaexam.controllers;

import com.example.javaexam.config.SecuredApiErrorResponses;
import com.example.javaexam.dtos.billing.BillResponse;
import com.example.javaexam.dtos.notification.NotificationResponse;
import com.example.javaexam.dtos.payment.PaymentResponse;
import com.example.javaexam.exceptions.ApiException;
import com.example.javaexam.models.Customer;
import com.example.javaexam.repositories.CustomerRepository;
import com.example.javaexam.services.BillingService;
import com.example.javaexam.services.NotificationService;
import com.example.javaexam.services.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.security.Principal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/customer")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CUSTOMER')")
@Tag(name = "Customer Portal")
@SecuredApiErrorResponses
public class CustomerPortalController {

    private final CustomerRepository customerRepository;
    private final BillingService billingService;
    private final PaymentService paymentService;
    private final NotificationService notificationService;

    @GetMapping("/bills")
    @Operation(summary = "View bills for the authenticated customer")
    public List<BillResponse> bills(Principal principal) {
        return billingService.listForCustomer(currentCustomer(principal).getId());
    }

    @GetMapping("/payments")
    @Operation(summary = "View payment history for the authenticated customer")
    public List<PaymentResponse> payments(Principal principal) {
        return paymentService.listForCustomer(currentCustomer(principal).getId());
    }

    @GetMapping("/notifications")
    @Operation(summary = "View notifications for the authenticated customer")
    public List<NotificationResponse> notifications(Principal principal) {
        return notificationService.listForCurrentCustomer(principal.getName());
    }

    private Customer currentCustomer(Principal principal) {
        return customerRepository.findByProfileEmailIgnoreCase(principal.getName())
                .orElseThrow(() -> ApiException.notFound("Customer account not found"));
    }
}
