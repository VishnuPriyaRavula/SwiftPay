package com.swiftpay.api;

import com.swiftpay.domain.Payment;
import com.swiftpay.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/v1/payments")
@Tag(name = "Payments", description = "P2P payment gateway operations")
public class PaymentController {
    private final PaymentService paymentService;
    public PaymentController(PaymentService paymentService) { this.paymentService = paymentService; }

    @PostMapping
    @Operation(summary = "Initiate a payment", description = "Creates an idempotent payment and publishes a ledger event")
    public ResponseEntity<PaymentResponse> create(@RequestHeader(value = "Idempotency-Key", required = false) UUID idempotencyKey,
                                                   @Valid @RequestBody PaymentRequest request) {
        UUID transactionId = idempotencyKey == null ? UUID.randomUUID() : idempotencyKey;
        Payment payment = paymentService.create(request, transactionId);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(PaymentResponse.from(payment));
    }

    @GetMapping("/{transactionId}")
    public PaymentResponse get(@PathVariable UUID transactionId) { return PaymentResponse.from(paymentService.get(transactionId)); }
}
