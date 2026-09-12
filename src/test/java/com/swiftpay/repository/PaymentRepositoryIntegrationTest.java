package com.swiftpay.repository;

import com.swiftpay.domain.Payment;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers(disabledWithoutDocker = true)
class PaymentRepositoryIntegrationTest {
    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    @Autowired PaymentRepository repository;

    @Test
    void persistsPaymentAndFindsUserHistory() {
        UUID sender = UUID.randomUUID();
        UUID receiver = UUID.randomUUID();
        Payment payment = repository.save(new Payment(UUID.randomUUID(), sender, receiver,
                new BigDecimal("4.25"), "USD"));

        assertThat(repository.findById(payment.getTransactionId())).isPresent();
        assertThat(repository.findBySenderIdOrReceiverIdOrderByCreatedAtDesc(sender, sender))
                .extracting(Payment::getTransactionId).containsExactly(payment.getTransactionId());
    }
}
