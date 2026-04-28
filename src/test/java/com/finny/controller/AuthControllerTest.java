package com.finny.controller;

import com.finny.dto.LoginRequest;
import com.finny.dto.LoginResponse;
import com.finny.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AuthControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
    }

    @Test
    void testLogin_Success() throws Exception {
        LoginResponse response = new LoginResponse("session-id-123");

        when(authService.loginUser(any(LoginRequest.class), eq("Test Browser"))).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"test@example.com\",\"password\":\"password123\"}")
                .header(HttpHeaders.USER_AGENT, "Test Browser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("session-id-123"));
    }

    @Test
    void testLogin_InvalidCredentials() throws Exception {
        when(authService.loginUser(any(LoginRequest.class), any()))
                .thenThrow(new IllegalArgumentException("Email or Password is invalid."));

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"test@example.com\",\"password\":\"wrong\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Email or Password is invalid."));
    }
}
