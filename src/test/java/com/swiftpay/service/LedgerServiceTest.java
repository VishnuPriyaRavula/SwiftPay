package com.swiftpay.service;

import com.swiftpay.domain.*;
import com.swiftpay.messaging.PaymentEvent;
import com.swiftpay.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LedgerServiceTest {
    @Mock PaymentRepository payments;
    @Mock UserAccountRepository accounts;
    @Mock KafkaTemplate<String, PaymentEvent> kafka;
    @InjectMocks LedgerService ledger;

    @Test
    void transfersFundsAtomicallyWhenBalanceIsEnough() {
        UUID transaction = UUID.randomUUID();
        UUID senderId = UUID.randomUUID();
        UUID receiverId = UUID.randomUUID();
        Payment payment = new Payment(transaction, senderId, receiverId, new BigDecimal("25.00"), "USD");
        UserAccount sender = new UserAccount(senderId, new BigDecimal("100.00"), "USD");
        UserAccount receiver = new UserAccount(receiverId, new BigDecimal("10.00"), "USD");
        when(payments.findById(transaction)).thenReturn(Optional.of(payment));
        when(accounts.findByIdForUpdate(senderId)).thenReturn(Optional.of(sender));
        when(accounts.findByIdForUpdate(receiverId)).thenReturn(Optional.of(receiver));

        ledger.process(new PaymentEvent(transaction, senderId, receiverId, new BigDecimal("25.00"), "USD", Instant.now(), null));

        assertThat(sender.getBalance()).isEqualByComparingTo("75.00");
        assertThat(receiver.getBalance()).isEqualByComparingTo("35.00");
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.COMPLETED);
        verify(kafka).send(eq("payment-completed"), eq(transaction.toString()), any(PaymentEvent.class));
    }

    @Test
    void recordsInsufficientFundsAsFailedWithoutDebit() {
        UUID transaction = UUID.randomUUID();
        UUID senderId = UUID.randomUUID();
        UUID receiverId = UUID.randomUUID();
        Payment payment = new Payment(transaction, senderId, receiverId, new BigDecimal("25.00"), "USD");
        UserAccount sender = new UserAccount(senderId, new BigDecimal("10.00"), "USD");
        UserAccount receiver = new UserAccount(receiverId, BigDecimal.ZERO, "USD");
        when(payments.findById(transaction)).thenReturn(Optional.of(payment));
        when(accounts.findByIdForUpdate(senderId)).thenReturn(Optional.of(sender));
        when(accounts.findByIdForUpdate(receiverId)).thenReturn(Optional.of(receiver));

        ledger.process(new PaymentEvent(transaction, senderId, receiverId, new BigDecimal("25.00"), "USD", Instant.now(), null));

        assertThat(sender.getBalance()).isEqualByComparingTo("10.00");
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.FAILED);
        assertThat(payment.getFailureReason()).isEqualTo("Insufficient funds");
        verify(kafka).send(eq("payment-failed"), eq(transaction.toString()), any(PaymentEvent.class));
    }
}
