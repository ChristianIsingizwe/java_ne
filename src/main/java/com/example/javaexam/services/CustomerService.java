package com.example.javaexam.services;

import com.example.javaexam.dtos.common.StatusUpdateRequest;
import com.example.javaexam.dtos.customer.CustomerResponse;
import com.example.javaexam.dtos.customer.UpdateCustomerRequest;
import com.example.javaexam.exceptions.ApiException;
import com.example.javaexam.mappers.ApplicationMapper;
import com.example.javaexam.models.Customer;
import com.example.javaexam.repositories.CustomerRepository;
import com.example.javaexam.repositories.ProfileRepository;
import com.example.javaexam.services.contract.CustomerServiceContract;
import com.example.javaexam.utils.InputSanitizer;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomerService implements CustomerServiceContract {

    private final CustomerRepository customerRepository;
    private final ProfileRepository profileRepository;
    private final ApplicationMapper applicationMapper;

    public List<CustomerResponse> list() {
        return customerRepository.findAllByOrderByIdAsc().stream()
                .map(applicationMapper::toCustomerResponse)
                .toList();
    }

    public CustomerResponse get(Long customerId) {
        return applicationMapper.toCustomerResponse(findCustomer(customerId));
    }

    @Transactional
    public CustomerResponse update(Long customerId, UpdateCustomerRequest request) {
        Customer customer = findCustomer(customerId);
        String fullName = InputSanitizer.normalizeRequired(request.fullName(), "Full name");
        String nationalId = InputSanitizer.normalizeRequired(request.nationalId(), "National ID");
        String email = InputSanitizer.normalizeEmail(request.email());
        String phone = InputSanitizer.normalizeRwandanPhoneNumber(request.phoneNumber());
        String address = InputSanitizer.normalizeRequired(request.address(), "Address");

        ensureUniqueness(email, phone, nationalId, customer.getProfile().getId());

        customer.getProfile().setFullName(fullName);
        customer.getProfile().setNationalId(nationalId);
        customer.getProfile().setEmail(email);
        customer.getProfile().setPhoneNumber(phone);
        customer.getProfile().setAddress(address);
        customer.getProfile().setStatus(request.status());
        return applicationMapper.toCustomerResponse(customerRepository.save(customer));
    }

    @Transactional
    public CustomerResponse updateStatus(Long customerId, StatusUpdateRequest request) {
        Customer customer = findCustomer(customerId);
        customer.getProfile().setStatus(request.status());
        return applicationMapper.toCustomerResponse(customerRepository.save(customer));
    }

    private Customer findCustomer(Long customerId) {
        return customerRepository.findById(customerId)
                .orElseThrow(() -> ApiException.notFound("Customer not found"));
    }

    private void ensureUniqueness(String email, String phone, String nationalId, Long profileId) {
        profileRepository.findByEmailIgnoreCase(email)
                .filter(profile -> !profile.getId().equals(profileId))
                .ifPresent(profile -> {
                    throw ApiException.conflict("Email already exists");
                });
        for (String variant : InputSanitizer.rwandanPhoneVariants(phone)) {
            profileRepository.findByPhoneNumber(variant)
                    .filter(profile -> !profile.getId().equals(profileId))
                    .ifPresent(profile -> {
                        throw ApiException.conflict("Phone number already exists");
                    });
        }
        profileRepository.findByNationalId(nationalId)
                .filter(profile -> !profile.getId().equals(profileId))
                .ifPresent(profile -> {
                    throw ApiException.conflict("National ID already exists");
                });
    }
}
