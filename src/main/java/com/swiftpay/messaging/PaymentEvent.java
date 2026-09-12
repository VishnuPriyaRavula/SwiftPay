package com.swiftpay.messaging;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PaymentEvent(UUID transactionId, UUID senderId, UUID receiverId, BigDecimal amount,
                           String currency, Instant occurredAt, String reason) {
    public static PaymentEvent initiated(UUID transactionId, UUID senderId, UUID receiverId,
                                         BigDecimal amount, String currency) {
        return new PaymentEvent(transactionId, senderId, receiverId, amount, currency, Instant.now(), null);
    }
    public static PaymentEvent result(PaymentEvent source, String reason) {
        return new PaymentEvent(source.transactionId(), source.senderId(), source.receiverId(), source.amount(),
                source.currency(), Instant.now(), reason);
    }
}
