package com.finny.controller;

import com.finny.dto.CreateAccountRequest;
import com.finny.service.AccountService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AccountControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AccountService accountService;

    @InjectMocks
    private AccountController accountController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(accountController).build();
    }

    @Test
    void testCreateAccount_Success() throws Exception {
        when(accountService.createAccount(any(CreateAccountRequest.class))).thenReturn("acc-123");

        String json = "{" +
                "\"tenantId\":\"tenant-123\"," +
                "\"userId\":\"user-123\"," +
                "\"name\":\"My Checking\"," +
                "\"type\":\"BANK\"," +
                "\"currencyCode\":\"USD\"," +
                "\"balance\":500.0" +
                "}";

        mockMvc.perform(post("/api/v1/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isCreated())
                .andExpect(content().string("Account created successfully with ID: acc-123"));
    }

    @Test
    void testCreateAccount_ValidationError() throws Exception {
        when(accountService.createAccount(any(CreateAccountRequest.class)))
                .thenThrow(new IllegalArgumentException("Invalid currency code: XYZ"));

        String json = "{" +
                "\"tenantId\":\"tenant-123\"," +
                "\"userId\":\"user-123\"," +
                "\"name\":\"My Checking\"," +
                "\"type\":\"BANK\"," +
                "\"currencyCode\":\"XYZ\"," +
                "\"balance\":500.0" +
                "}";

        mockMvc.perform(post("/api/v1/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid currency code: XYZ"));
    }
}
