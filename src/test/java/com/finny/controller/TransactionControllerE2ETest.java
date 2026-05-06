package com.finny.controller;

import com.finny.BaseMySqlTest;
import com.finny.domain.Account;
import com.finny.domain.Category;
import com.finny.domain.Transaction;
import com.finny.domain.enums.AccountStatus;
import com.finny.domain.enums.AccountType;
import com.finny.domain.enums.TransactionType;
import com.finny.dto.LoginRequest;
import com.finny.dto.LoginResponse;
import com.finny.dto.RegisterRequest;
import com.finny.dto.PaginatedResponse;
import com.finny.dto.TransactionDto;
import com.finny.dto.UpdateTransactionRequest;
import com.finny.repository.AccountRepository;
import com.finny.repository.CategoryRepository;
import com.finny.repository.TransactionRepository;
import com.finny.repository.master.UserRepository;
import com.finny.domain.master.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TransactionControllerE2ETest extends BaseMySqlTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    private final RestTemplate restTemplate = new RestTemplate();

    private String getBaseUrl() {
        return "http://localhost:" + port;
    }

    private String token;
    private Account account;
    private Category category;

    @BeforeEach
    void setUpData() {
        String suffix = String.valueOf(System.currentTimeMillis()).substring(8);
        String email = "tx-e2e-" + suffix + "@example.com";
        String password = "password123";

        // Register new user for existing tenant
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setTenantId("family_smith_id");
        registerRequest.setEmail(email);
        registerRequest.setPassword(password);
        registerRequest.setFirstName("TX");
        registerRequest.setLastName("User");
        
        restTemplate.postForEntity(getBaseUrl() + "/api/v1/auth/register", registerRequest, String.class);

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail(email);
        loginRequest.setPassword(password);
        
        ResponseEntity<LoginResponse> loginResponse = restTemplate.postForEntity(getBaseUrl() + "/api/v1/auth/login", loginRequest, LoginResponse.class);
        token = loginResponse.getBody().getToken();

        User user = userRepository.findByEmail(email).orElseThrow();
        String userId = user.getId();

        // Prepare test data
        setupTenantContext(userId, "family_smith_id");
        
        account = new Account();
        account.setName("GET API Test Account");
        account.setType(AccountType.CASH);
        account.setCurrency("USD");
        account.setBalance(BigDecimal.valueOf(1000.00));
        account.setStatus(AccountStatus.ACTIVE);
        account = accountRepository.save(account);

        category = new Category();
        category.setName("GET API Category");
        category = categoryRepository.save(category);

        for (int i = 1; i <= 15; i++) {
            Transaction tx = new Transaction();
            tx.setAccount(account);
            tx.setCategory(category);
            tx.setAmount(BigDecimal.valueOf(i * 10));
            tx.setType(TransactionType.SPENDING);
            tx.setTransactionDate(LocalDateTime.now().minusDays(i));
            tx.setDescription("Transaction " + i);
            transactionRepository.save(tx);
        }
    }

    @AfterEach
    void cleanUp() {
        // We already have the context setup for the active transaction, but just in case:
        if (account != null) {
            transactionRepository.deleteAll();
            categoryRepository.delete(category);
            accountRepository.delete(account);
        }
    }

    @Test
    void testGetTransactions_PaginationAndSorting() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<?> entity = new HttpEntity<>(headers);

        // Fetch page 0, size 5
        ResponseEntity<PaginatedResponse<TransactionDto>> response = restTemplate.exchange(
                getBaseUrl() + "/api/v1/transactions?page=0&size=5",
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<PaginatedResponse<TransactionDto>>() {}
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        PaginatedResponse<TransactionDto> body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getContent()).hasSize(5);
        assertThat(body.getTotalElements()).isGreaterThanOrEqualTo(15);
        
        // Verify sorting (descending by date)
        LocalDateTime date1 = body.getContent().get(0).getTransactionDate();
        LocalDateTime date2 = body.getContent().get(1).getTransactionDate();
        assertThat(date1).isAfter(date2);
    }

    @Test
    void testGetTransactions_WithFilters() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<?> entity = new HttpEntity<>(headers);

        // Filter by accountId
        ResponseEntity<PaginatedResponse<TransactionDto>> response = restTemplate.exchange(
                getBaseUrl() + "/api/v1/transactions?accountId=" + account.getId(),
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<PaginatedResponse<TransactionDto>>() {}
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        PaginatedResponse<TransactionDto> body = response.getBody();
        assertThat(body.getContent()).isNotEmpty();
        assertThat(body.getContent().get(0).getAccountId()).isEqualTo(account.getId());
    }

    @Test
    void testDeleteTransaction_Success() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        // 1. Create a transaction
        // Start balance: 1000
        // Spent: 15 transactions of 10, 20, ..., 150 = 1200? 
        // Wait, 15*16/2 * 10 = 120 * 10 = 1200.
        // So balance should be 1000 - 1200 = -200?
        // Let's check the balance after setup.
        
        Account initialAcc = accountRepository.findById(account.getId()).orElseThrow();
        BigDecimal currentBalance = initialAcc.getBalance();

        Transaction tx = new Transaction();
        tx.setAccount(initialAcc);
        tx.setCategory(category);
        tx.setAmount(new BigDecimal("50.00"));
        tx.setType(TransactionType.INCOME);
        tx.setTransactionDate(LocalDateTime.now());
        tx.setDescription("To be deleted");
        tx = transactionRepository.save(tx);
        
        // Manually update balance to simulate what service would do (or we could call the API to create it)
        initialAcc.setBalance(currentBalance.add(new BigDecimal("50.00")));
        accountRepository.save(initialAcc);

        // 2. Delete via API
        HttpEntity<?> entity = new HttpEntity<>(headers);
        ResponseEntity<Void> response = restTemplate.exchange(
                getBaseUrl() + "/api/v1/transactions/" + tx.getId(),
                HttpMethod.DELETE,
                entity,
                Void.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        // 3. Verify deletion and balance reversal
        assertThat(transactionRepository.existsById(tx.getId())).isFalse();
        Account finalAcc = accountRepository.findById(account.getId()).orElseThrow();
        assertThat(finalAcc.getBalance()).isEqualByComparingTo(currentBalance);
    }

    @Test
    void testUpdateTransaction_Success() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        // 1. Create a transaction
        Account initialAcc = accountRepository.findById(account.getId()).orElseThrow();
        BigDecimal currentBalance = initialAcc.getBalance();

        Transaction tx = new Transaction();
        tx.setAccount(initialAcc);
        tx.setCategory(category);
        tx.setAmount(new BigDecimal("100.00"));
        tx.setType(TransactionType.INCOME);
        tx.setTransactionDate(LocalDateTime.now());
        tx.setDescription("Original");
        tx = transactionRepository.save(tx);
        
        initialAcc.setBalance(currentBalance.add(new BigDecimal("100.00")));
        accountRepository.save(initialAcc);

        // 2. Update via API: Change Income 100 to Spending 50
        UpdateTransactionRequest request = new UpdateTransactionRequest();
        request.setId(tx.getId());
        request.setAccountId(account.getId());
        request.setCategoryId(category.getId());
        request.setAmount(new BigDecimal("50.00"));
        request.setType(TransactionType.SPENDING);
        request.setTransactionDateTime(LocalDateTime.now());
        request.setDescription("Updated");

        HttpEntity<UpdateTransactionRequest> entity = new HttpEntity<>(request, headers);
        ResponseEntity<String> response = restTemplate.exchange(
                getBaseUrl() + "/api/v1/transactions/" + tx.getId(),
                HttpMethod.PUT,
                entity,
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        // 3. Verify balance reversal and re-application
        // Original: currentBalance + 100
        // Update Logic: (currentBalance + 100) - 100 [reverse] - 50 [apply new spending] = currentBalance - 50
        Account finalAcc = accountRepository.findById(account.getId()).orElseThrow();
        assertThat(finalAcc.getBalance()).isEqualByComparingTo(currentBalance.subtract(new BigDecimal("50.00")));
        
        Transaction updatedTx = transactionRepository.findById(tx.getId()).orElseThrow();
        assertThat(updatedTx.getDescription()).isEqualTo("Updated");
        assertThat(updatedTx.getType()).isEqualTo(TransactionType.SPENDING);
    }
}
