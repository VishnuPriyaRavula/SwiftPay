package com.swiftpay.domain;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;

class PaymentTest {
    @Test
    void newPaymentStartsPendingAndCanComplete() {
        Payment payment = new Payment(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), BigDecimal.TEN, "usd");
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PENDING);
        payment.complete();
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.COMPLETED);
        assertThat(payment.getCompletedAt()).isNotNull();
    }

    @Test
    void failedPaymentRetainsReason() {
        Payment payment = new Payment(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), BigDecimal.TEN, "USD");
        payment.fail("Insufficient funds");
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.FAILED);
        assertThat(payment.getFailureReason()).isEqualTo("Insufficient funds");
    }
}
