package com.example.javaexam.controllers;

import com.example.javaexam.config.SecuredApiErrorResponses;
import com.example.javaexam.dtos.billing.BillResponse;
import com.example.javaexam.dtos.billing.GenerateBillRequest;
import com.example.javaexam.services.contract.BillingServiceContract;
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
@RequestMapping("/api/bills")
@RequiredArgsConstructor
@Tag(name = "Bills")
@SecuredApiErrorResponses
public class BillingController {

    private final BillingServiceContract billingService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE')")
    @Operation(summary = "List all bills")
    public List<BillResponse> list() {
        return billingService.list();
    }

    @GetMapping("/{billReference}")
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE')")
    @Operation(summary = "Get a bill by reference")
    public BillResponse get(@PathVariable String billReference) {
        return billingService.getByReference(billReference);
    }

    @PostMapping("/generate")
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE')")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Generate a bill from a meter reading")
    public BillResponse generate(@Valid @RequestBody GenerateBillRequest request, Principal principal) {
        return billingService.generate(request, principal.getName());
    }

    @PostMapping("/{billId}/approve")
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE')")
    @Operation(summary = "Approve a generated bill")
    public BillResponse approve(@PathVariable Long billId, Principal principal) {
        return billingService.approve(billId, principal.getName());
    }

    @PostMapping("/{billId}/late-penalty")
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE')")
    @Operation(summary = "Apply late payment penalty to a bill")
    public BillResponse applyLatePenalty(@PathVariable Long billId) {
        return billingService.applyLatePenalty(billId);
    }
}
