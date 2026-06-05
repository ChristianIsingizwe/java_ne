package com.example.javaexam.controllers;

import com.example.javaexam.config.SecuredApiErrorResponses;
import com.example.javaexam.dtos.payment.PaymentResponse;
import com.example.javaexam.dtos.payment.RecordPaymentRequest;
import com.example.javaexam.services.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Tag(name = "Payments")
@SecuredApiErrorResponses
public class PaymentController {

    private final PaymentService paymentService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE')")
    @Operation(summary = "List payment records")
    public List<PaymentResponse> list() {
        return paymentService.list();
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE')")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Record a bill payment")
    public PaymentResponse record(@Valid @RequestBody RecordPaymentRequest request, Principal principal) {
        return paymentService.record(request, principal.getName());
    }

    @PostMapping("/{paymentId}/approve")
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE')")
    @Operation(summary = "Approve a recorded payment")
    public PaymentResponse approve(@PathVariable Long paymentId, Principal principal) {
        return paymentService.approve(paymentId, principal.getName());
    }
}
