package com.swiftpay.api;

import com.swiftpay.domain.*;
import com.swiftpay.service.PaymentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PaymentController.class)
class PaymentControllerTest {
    @Autowired MockMvc mvc;
    @MockBean PaymentService service;

    @Test
    void acceptsValidPaymentRequest() throws Exception {
        UUID id = UUID.randomUUID();
        Payment payment = new Payment(id, UUID.randomUUID(), UUID.randomUUID(), new BigDecimal("10.00"), "USD");
        when(service.create(any(), eq(id))).thenReturn(payment);
        mvc.perform(post("/v1/payments").header("Idempotency-Key", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"senderId\":\"" + payment.getSenderId() + "\",\"receiverId\":\"" + payment.getReceiverId() + "\",\"amount\":10,\"currency\":\"USD\"}"))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.transactionId").value(id.toString()))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void rejectsInvalidAmount() throws Exception {
        mvc.perform(post("/v1/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"senderId\":\"" + UUID.randomUUID() + "\",\"receiverId\":\"" + UUID.randomUUID() + "\",\"amount\":0,\"currency\":\"USD\"}"))
                .andExpect(status().isBadRequest());
    }
}
