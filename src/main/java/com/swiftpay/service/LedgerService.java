package com.swiftpay.service;

import com.swiftpay.domain.Payment;
import com.swiftpay.domain.UserAccount;
import com.swiftpay.messaging.PaymentEvent;
import com.swiftpay.repository.PaymentRepository;
import com.swiftpay.repository.UserAccountRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class LedgerService {
    private final PaymentRepository paymentRepository;
    private final UserAccountRepository accountRepository;
    private final KafkaTemplate<String, PaymentEvent> kafkaTemplate;

    public LedgerService(PaymentRepository paymentRepository, UserAccountRepository accountRepository,
                         KafkaTemplate<String, PaymentEvent> kafkaTemplate) {
        this.paymentRepository = paymentRepository;
        this.accountRepository = accountRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @KafkaListener(topics = PaymentService.PAYMENT_INITIATED_TOPIC, groupId = "swiftpay-ledger")
    @Transactional
    public void process(PaymentEvent event) {
        Payment payment = paymentRepository.findById(event.transactionId()).orElseThrow();
        if (payment.getStatus() != com.swiftpay.domain.PaymentStatus.PENDING) return;
        try {
            if (event.senderId().equals(event.receiverId())) throw new IllegalArgumentException("Sender and receiver must differ");
            UserAccount sender = accountRepository.findByIdForUpdate(event.senderId()).orElseThrow(() -> new IllegalArgumentException("Sender account not found"));
            UserAccount receiver = accountRepository.findByIdForUpdate(event.receiverId()).orElseThrow(() -> new IllegalArgumentException("Receiver account not found"));
            if (!sender.getCurrency().equalsIgnoreCase(event.currency()) || !receiver.getCurrency().equalsIgnoreCase(event.currency())) throw new IllegalArgumentException("Currency mismatch");
            if (sender.getBalance().compareTo(event.amount()) < 0) throw new IllegalArgumentException("Insufficient funds");
            sender.debit(event.amount());
            receiver.credit(event.amount());
            payment.complete();
            paymentRepository.save(payment);
            kafkaTemplate.send("payment-completed", event.transactionId().toString(), PaymentEvent.result(event, null));
        } catch (RuntimeException exception) {
            payment.fail(exception.getMessage());
            paymentRepository.save(payment);
            kafkaTemplate.send("payment-failed", event.transactionId().toString(), PaymentEvent.result(event, exception.getMessage()));
        }
    }
}
