package com.swiftpay.domain;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;

class UserAccountTest {
    @Test
    void debitAndCreditUpdateBalances() {
        UserAccount account = new UserAccount(UUID.randomUUID(), new BigDecimal("100.00"), "USD");
        account.debit(new BigDecimal("25.50"));
        account.credit(new BigDecimal("10.00"));
        assertThat(account.getBalance()).isEqualByComparingTo("84.50");
    }
}
