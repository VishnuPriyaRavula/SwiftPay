package com.swiftpay.analytics;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface PaymentAnalyticsRepository extends JpaRepository<PaymentAnalytics, UUID> {
}
