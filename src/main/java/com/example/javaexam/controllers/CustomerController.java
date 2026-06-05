package com.example.javaexam.controllers;

import com.example.javaexam.config.SecuredApiErrorResponses;
import com.example.javaexam.dtos.common.StatusUpdateRequest;
import com.example.javaexam.dtos.customer.CustomerResponse;
import com.example.javaexam.dtos.customer.UpdateCustomerRequest;
import com.example.javaexam.services.contract.CustomerServiceContract;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
@Tag(name = "Customers")
@SecuredApiErrorResponses
@Validated
public class CustomerController {

    private final CustomerServiceContract customerService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE','OPERATOR')")
    @Operation(summary = "List customers")
    public List<CustomerResponse> list() {
        return customerService.list();
    }

    @GetMapping("/{customerId}")
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE','OPERATOR')")
    @Operation(summary = "Get a customer by id")
    public CustomerResponse get(@PathVariable @Positive Long customerId) {
        return customerService.get(customerId);
    }

    @PutMapping("/{customerId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update a customer")
    public CustomerResponse update(@PathVariable @Positive Long customerId, @Valid @RequestBody UpdateCustomerRequest request) {
        return customerService.update(customerId, request);
    }

    @PatchMapping("/{customerId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Activate or deactivate a customer")
    public CustomerResponse updateStatus(
            @PathVariable @Positive Long customerId,
            @Valid @RequestBody StatusUpdateRequest request) {
        return customerService.updateStatus(customerId, request);
    }
}
