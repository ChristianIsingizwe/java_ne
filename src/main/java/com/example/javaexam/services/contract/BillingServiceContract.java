package com.example.javaexam.services.contract;

import com.example.javaexam.dtos.billing.BillResponse;
import com.example.javaexam.dtos.billing.GenerateBillRequest;
import java.util.List;

public interface BillingServiceContract {

    BillResponse generate(GenerateBillRequest request, String generatedByEmail);

    BillResponse approve(Long billId, String approverEmail);

    BillResponse applyLatePenalty(Long billId);

    List<BillResponse> list();

    List<BillResponse> listForCustomer(Long customerId);

    BillResponse getByReference(String billReference);
}
