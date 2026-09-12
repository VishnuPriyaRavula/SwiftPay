package com.swiftpay.analytics;

import com.swiftpay.messaging.PaymentEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AnalyticsWorker {
    private final PaymentAnalyticsRepository repository;

    public AnalyticsWorker(PaymentAnalyticsRepository repository) {
        this.repository = repository;
    }

    @KafkaListener(topics = "payment-completed", groupId = "swiftpay-analytics", concurrency = "6")
    @Transactional
    public void consume(PaymentEvent event) {
        if (!repository.existsById(event.transactionId())) {
            repository.save(new PaymentAnalytics(event.transactionId(), event.amount(), event.currency(), event.occurredAt()));
        }
    }
}
