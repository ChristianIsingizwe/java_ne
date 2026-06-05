package com.example.javaexam.services.contract;

import com.example.javaexam.dtos.payment.PaymentResponse;
import com.example.javaexam.dtos.payment.RecordPaymentRequest;
import java.util.List;

public interface PaymentServiceContract {

    PaymentResponse record(RecordPaymentRequest request, String recordedByEmail);

    PaymentResponse approve(Long paymentId, String approverEmail);

    List<PaymentResponse> list();

    List<PaymentResponse> listForCustomer(Long customerId);
}
