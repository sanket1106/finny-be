package com.finny.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.finny.domain.enums.TransactionType;
import com.finny.dto.CreateTransactionRequest;
import com.finny.dto.CreateTransactionResponse;
import com.finny.service.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class TransactionControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private TransactionService transactionService;

    @InjectMocks
    private TransactionController transactionController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(transactionController).build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    void testCreateTransaction_Success() throws Exception {
        CreateTransactionRequest request = new CreateTransactionRequest();
        request.setAccountId("acc-1");
        request.setCategoryId("cat-1");
        request.setAmount(new BigDecimal("100.00"));
        request.setType(TransactionType.SPENDING);
        request.setTransactionDateTime(LocalDateTime.now());

        CreateTransactionResponse response = new CreateTransactionResponse("tx-123");
        when(transactionService.createTransaction(any(CreateTransactionRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.transactionId").value("tx-123"));
    }

    @Test
    void testCreateTransaction_BadRequest() throws Exception {
        CreateTransactionRequest request = new CreateTransactionRequest();
        // Missing mandatory fields

        mockMvc.perform(post("/api/v1/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateTransaction_AmountValidation_Failure() throws Exception {
        CreateTransactionRequest request = new CreateTransactionRequest();
        request.setAccountId("acc-1");
        request.setCategoryId("cat-1");
        request.setAmount(new BigDecimal("100.555")); // Invalid: 3 decimals
        request.setType(TransactionType.SPENDING);
        request.setTransactionDateTime(LocalDateTime.now());

        mockMvc.perform(post("/api/v1/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateTransaction_ServiceException() throws Exception {
        CreateTransactionRequest request = new CreateTransactionRequest();
        request.setAccountId("acc-1");
        request.setCategoryId("cat-1");
        request.setAmount(new BigDecimal("100.00"));
        request.setType(TransactionType.SPENDING);
        request.setTransactionDateTime(LocalDateTime.now());

        when(transactionService.createTransaction(any(CreateTransactionRequest.class)))
                .thenThrow(new IllegalArgumentException("Account not found"));

        mockMvc.perform(post("/api/v1/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Account not found"));
    }
}
