package com.swiftpay.analytics;

import com.swiftpay.messaging.PaymentEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnalyticsWorkerTest {
    @Mock PaymentAnalyticsRepository repository;

    @Test
    void storesCompletedEventOnce() {
        UUID transactionId = UUID.randomUUID();
        when(repository.existsById(transactionId)).thenReturn(false);
        new AnalyticsWorker(repository).consume(new PaymentEvent(transactionId, UUID.randomUUID(), UUID.randomUUID(),
                BigDecimal.TEN, "USD", Instant.now(), null));
        verify(repository).save(any(PaymentAnalytics.class));
    }

    @Test
    void ignoresDuplicateCompletedEvent() {
        UUID transactionId = UUID.randomUUID();
        when(repository.existsById(transactionId)).thenReturn(true);
        new AnalyticsWorker(repository).consume(new PaymentEvent(transactionId, UUID.randomUUID(), UUID.randomUUID(),
                BigDecimal.TEN, "USD", Instant.now(), null));
        verify(repository, never()).save(any(PaymentAnalytics.class));
    }
}
