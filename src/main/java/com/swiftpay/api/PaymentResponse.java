package com.swiftpay.api;

import com.swiftpay.domain.Payment;
import com.swiftpay.domain.PaymentStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PaymentResponse(UUID transactionId, UUID senderId, UUID receiverId, BigDecimal amount,
                              String currency, PaymentStatus status, Instant createdAt,
                              Instant completedAt, String failureReason) {
    public static PaymentResponse from(Payment payment) {
        return new PaymentResponse(payment.getTransactionId(), payment.getSenderId(), payment.getReceiverId(),
                payment.getAmount(), payment.getCurrency(), payment.getStatus(), payment.getCreatedAt(),
                payment.getCompletedAt(), payment.getFailureReason());
    }
}
