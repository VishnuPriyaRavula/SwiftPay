package com.swiftpay.service;

import com.swiftpay.api.PaymentRequest;
import com.swiftpay.domain.Payment;
import com.swiftpay.messaging.PaymentEvent;
import com.swiftpay.repository.PaymentRepository;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.UUID;

@Service
public class PaymentService {
    public static final String PAYMENT_INITIATED_TOPIC = "payment-initiated";
    private static final Duration IDEMPOTENCY_WINDOW = Duration.ofHours(24);
    private final PaymentRepository paymentRepository;
    private final StringRedisTemplate redis;
    private final KafkaTemplate<String, PaymentEvent> kafkaTemplate;

    public PaymentService(PaymentRepository paymentRepository, StringRedisTemplate redis,
                          KafkaTemplate<String, PaymentEvent> kafkaTemplate) {
        this.paymentRepository = paymentRepository;
        this.redis = redis;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Transactional
    public Payment create(PaymentRequest request, UUID transactionId) {
        String key = "payment:idempotency:" + transactionId;
        Boolean firstRequest = redis.opsForValue().setIfAbsent(key, "accepted", IDEMPOTENCY_WINDOW);
        if (Boolean.FALSE.equals(firstRequest)) {
            return paymentRepository.findById(transactionId)
                    .orElseThrow(() -> new DuplicatePaymentException(transactionId));
        }
        Payment payment = paymentRepository.save(new Payment(transactionId, request.senderId(), request.receiverId(),
                request.amount(), request.currency().toUpperCase()));
        kafkaTemplate.send(PAYMENT_INITIATED_TOPIC, transactionId.toString(),
                PaymentEvent.initiated(transactionId, request.senderId(), request.receiverId(), request.amount(),
                        request.currency().toUpperCase()));
        return payment;
    }

    public Payment get(UUID transactionId) {
        return paymentRepository.findById(transactionId)
                .orElseThrow(() -> new PaymentNotFoundException(transactionId));
    }

    public static class PaymentNotFoundException extends RuntimeException {
        public PaymentNotFoundException(UUID id) { super("Payment not found: " + id); }
    }
    public static class DuplicatePaymentException extends RuntimeException {
        public DuplicatePaymentException(UUID id) { super("Duplicate payment is still being processed: " + id); }
    }
}
