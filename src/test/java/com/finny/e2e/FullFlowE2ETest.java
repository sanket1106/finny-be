package com.finny.e2e;

import com.finny.BaseMySqlTest;
import com.finny.domain.enums.AccountType;
import com.finny.dto.*;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class FullFlowE2ETest extends BaseMySqlTest {

    @LocalServerPort
    private int port;

    private final RestTemplate restTemplate = new RestTemplate();

    private String getBaseUrl() {
        return "http://localhost:" + port;
    }

    @Test
    void testFullFlow_RegisterLoginAndCreateAccount() {
        String suffix = String.valueOf(System.currentTimeMillis()).substring(8);
        String tenantId = "family_smith_id"; // Use existing tenant
        String email = "e2e-" + suffix + "@example.com";
        String password = "password123";

        // 1. Register new user for existing tenant
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setTenantId(tenantId);
        registerRequest.setEmail(email);
        registerRequest.setPassword(password);
        registerRequest.setFirstName("E2E");
        registerRequest.setLastName("User");
        
        ResponseEntity<String> registerResponse = restTemplate.postForEntity(getBaseUrl() + "/api/v1/auth/register", registerRequest, String.class);
        assertThat(registerResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        // 2. Login
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail(email);
        loginRequest.setPassword(password);
        
        ResponseEntity<LoginResponse> loginResponse = restTemplate.postForEntity(getBaseUrl() + "/api/v1/auth/login", loginRequest, LoginResponse.class);
        assertThat(loginResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        String token = loginResponse.getBody().getToken();
        assertThat(token).isNotNull();

        // 3. Create Account
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        CreateAccountRequest accountRequest = new CreateAccountRequest();
        accountRequest.setUserId(email);
        accountRequest.setName("E2E Savings " + suffix);
        accountRequest.setType(AccountType.BANK);
        accountRequest.setCurrencyCode("USD");
        accountRequest.setBalance(new BigDecimal("5000.00"));

        HttpEntity<CreateAccountRequest> entity = new HttpEntity<>(accountRequest, headers);
        ResponseEntity<String> accountResponse = restTemplate.postForEntity(getBaseUrl() + "/api/v1/accounts", entity, String.class);
        assertThat(accountResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(accountResponse.getBody()).contains("Account created successfully");
    }
}
