package com.swiftpay.analytics;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/v1/analytics")
@Tag(name = "Analytics", description = "Real-time payment volume reporting")
public class AnalyticsController {
    private final PaymentAnalyticsRepository repository;
    public AnalyticsController(PaymentAnalyticsRepository repository) { this.repository = repository; }

    @GetMapping("/volume")
    @Operation(summary = "Get completed payment volume")
    public Map<String, Object> volume() {
        return Map.of("completedPayments", repository.count(),
                "totalAmount", repository.findAll().stream().map(PaymentAnalytics::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add));
    }
}
