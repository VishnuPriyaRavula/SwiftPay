package com.swiftpay.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "payments", indexes = {
        @Index(name = "idx_payment_sender_created", columnList = "sender_id,created_at"),
        @Index(name = "idx_payment_receiver_created", columnList = "receiver_id,created_at")
})
public class Payment {
    @Id
    private UUID transactionId;
    @Column(name = "sender_id", nullable = false)
    private UUID senderId;
    @Column(name = "receiver_id", nullable = false)
    private UUID receiverId;
    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;
    @Column(nullable = false, length = 3)
    private String currency;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus status;
    @Column(nullable = false)
    private Instant createdAt;
    private Instant completedAt;
    @Column(length = 500)
    private String failureReason;

    protected Payment() { }

    public Payment(UUID transactionId, UUID senderId, UUID receiverId, BigDecimal amount, String currency) {
        this.transactionId = transactionId;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.amount = amount;
        this.currency = currency;
        this.status = PaymentStatus.PENDING;
        this.createdAt = Instant.now();
    }

    public UUID getTransactionId() { return transactionId; }
    public UUID getSenderId() { return senderId; }
    public UUID getReceiverId() { return receiverId; }
    public BigDecimal getAmount() { return amount; }
    public String getCurrency() { return currency; }
    public PaymentStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getCompletedAt() { return completedAt; }
    public String getFailureReason() { return failureReason; }
    public void complete() { status = PaymentStatus.COMPLETED; completedAt = Instant.now(); failureReason = null; }
    public void fail(String reason) { status = PaymentStatus.FAILED; failureReason = reason; }
}
