package com.swiftpay.service;

import com.swiftpay.api.PaymentRequest;
import com.swiftpay.domain.Payment;
import com.swiftpay.messaging.PaymentEvent;
import com.swiftpay.repository.PaymentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {
    @Mock PaymentRepository paymentRepository;
    @Mock StringRedisTemplate redis;
    @Mock ValueOperations<String, String> values;
    @Mock KafkaTemplate<String, PaymentEvent> kafka;
    @InjectMocks PaymentService service;

    @Test
    void createsPendingPaymentAndPublishesInitiatedEvent() {
        UUID transactionId = UUID.randomUUID();
        UUID sender = UUID.randomUUID();
        UUID receiver = UUID.randomUUID();
        when(redis.opsForValue()).thenReturn(values);
        when(values.setIfAbsent(anyString(), eq("accepted"), any())).thenReturn(true);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Payment result = service.create(new PaymentRequest(sender, receiver, new BigDecimal("12.50"), "USD"), transactionId);

        assertThat(result.getTransactionId()).isEqualTo(transactionId);
        assertThat(result.getStatus()).isEqualTo(com.swiftpay.domain.PaymentStatus.PENDING);
        verify(kafka).send(eq(PaymentService.PAYMENT_INITIATED_TOPIC), eq(transactionId.toString()), any(PaymentEvent.class));
    }
}
