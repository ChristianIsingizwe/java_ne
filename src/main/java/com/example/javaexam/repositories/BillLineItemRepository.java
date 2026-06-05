package com.example.javaexam.repositories;

import com.example.javaexam.models.BillLineItem;
import com.example.javaexam.models.enums.BillLineType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BillLineItemRepository extends JpaRepository<BillLineItem, Long> {
    List<BillLineItem> findByBillIdOrderByDisplayOrderAsc(Long billId);
    Optional<BillLineItem> findByBillIdAndLineType(Long billId, BillLineType lineType);
}
