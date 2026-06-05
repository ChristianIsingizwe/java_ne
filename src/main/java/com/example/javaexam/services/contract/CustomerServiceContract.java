package com.example.javaexam.services.contract;

import com.example.javaexam.dtos.common.StatusUpdateRequest;
import com.example.javaexam.dtos.customer.CustomerResponse;
import com.example.javaexam.dtos.customer.UpdateCustomerRequest;
import java.util.List;

public interface CustomerServiceContract {

    List<CustomerResponse> list();

    CustomerResponse get(Long customerId);

    CustomerResponse update(Long customerId, UpdateCustomerRequest request);

    CustomerResponse updateStatus(Long customerId, StatusUpdateRequest request);
}
