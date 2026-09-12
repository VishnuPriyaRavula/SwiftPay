package com.swiftpay.analytics;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "payment_analytics", indexes = @Index(name = "idx_analytics_occurred_at", columnList = "occurred_at"))
public class PaymentAnalytics {
    @Id
    private UUID transactionId;
    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;
    @Column(nullable = false, length = 3)
    private String currency;
    @Column(nullable = false)
    private Instant occurredAt;

    protected PaymentAnalytics() { }
    public PaymentAnalytics(UUID transactionId, BigDecimal amount, String currency, Instant occurredAt) {
        this.transactionId = transactionId;
        this.amount = amount;
        this.currency = currency;
        this.occurredAt = occurredAt;
    }
    public UUID getTransactionId() { return transactionId; }
    public BigDecimal getAmount() { return amount; }
    public String getCurrency() { return currency; }
    public Instant getOccurredAt() { return occurredAt; }
}
