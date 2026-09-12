package com.swiftpay.repository;

import com.swiftpay.domain.Payment;
import com.swiftpay.domain.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    List<Payment> findBySenderIdOrReceiverIdOrderByCreatedAtDesc(UUID senderId, UUID receiverId);
    long countByStatus(PaymentStatus status);
}
