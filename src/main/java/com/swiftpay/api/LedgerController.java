package com.swiftpay.api;

import com.swiftpay.domain.Payment;
import com.swiftpay.repository.PaymentRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/ledger")
@Tag(name = "Ledger", description = "Payment reporting operations")
public class LedgerController {
    private final PaymentRepository paymentRepository;
    public LedgerController(PaymentRepository paymentRepository) { this.paymentRepository = paymentRepository; }
    @GetMapping("/{userId}/payments")
    @Operation(summary = "Get payment history for a user")
    public List<PaymentResponse> history(@PathVariable UUID userId) {
        return paymentRepository.findBySenderIdOrReceiverIdOrderByCreatedAtDesc(userId, userId).stream().map(PaymentResponse::from).toList();
    }
}
