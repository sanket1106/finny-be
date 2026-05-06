package com.finny.controller;

import com.finny.BaseMySqlTest;
import com.finny.domain.Account;
import com.finny.domain.enums.AccountStatus;
import com.finny.domain.enums.AccountType;
import com.finny.dto.ImportResponseDto;
import com.finny.dto.LoginRequest;
import com.finny.dto.LoginResponse;
import com.finny.dto.RegisterRequest;
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
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TransactionControllerE2EImportTest extends BaseMySqlTest {

    @LocalServerPort
    private int port;

    private final RestTemplate restTemplate = new RestTemplate();

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    private String token;
    private Account account;

    private String getBaseUrl() {
        return "http://localhost:" + port;
    }

    @BeforeEach
    void setUpData() {
        String suffix = String.valueOf(System.currentTimeMillis()).substring(8);
        String email = "import-e2e-" + suffix + "@example.com";
        String password = "password123";

        // Register new user for existing tenant
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setTenantId("family_smith_id");
        registerRequest.setEmail(email);
        registerRequest.setPassword(password);
        registerRequest.setFirstName("Import");
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
        account.setName("Import API Test Account");
        account.setType(AccountType.CASH);
        account.setCurrency("USD");
        account.setBalance(BigDecimal.valueOf(100.00)); // Should be reset to 0 before import
        account.setStatus(AccountStatus.ACTIVE);
        account = accountRepository.save(account);
    }

    @AfterEach
    void cleanUp() {
        if (account != null) {
            transactionRepository.deleteAll();
            categoryRepository.deleteAll();
            accountRepository.delete(account);
        }
    }

    @Test
    void testImportTransactions_Success() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        String csvData = "transaction date,amount,transaction type,account name,account id,to account name,to account id,category name,subcategory name,description\n" +
                "01-01-2024,500.00,INCOME,," + account.getId() + ",,,Salary,Base Salary,January Salary\n" +
                "02-01-2024,50.00,SPENDING,," + account.getId() + ",,,Food,Groceries,Walmart\n" +
                "03-01-2024,invalid_amount,SPENDING,," + account.getId() + ",,,Shopping,,Invalid Row\n";

        ByteArrayResource resource = new ByteArrayResource(csvData.getBytes(StandardCharsets.UTF_8)) {
            @Override
            public String getFilename() {
                return "test.csv";
            }
        };

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", resource);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        ResponseEntity<ImportResponseDto> response = restTemplate.postForEntity(
                getBaseUrl() + "/api/v1/transactions/import",
                requestEntity,
                ImportResponseDto.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        ImportResponseDto dto = response.getBody();
        assertThat(dto).isNotNull();
        assertThat(dto.getTotalRows()).isEqualTo(3);
        assertThat(dto.getSuccessfulRows()).isEqualTo(2);
        assertThat(dto.getFailedRows()).isEqualTo(1);
        assertThat(dto.getErrors()).hasSize(1);
        assertThat(dto.getErrors().get(0)).contains("Row 4"); // 1-index header=1, row1=2, row2=3, row3=4

        assertThat(transactionRepository.count()).isEqualTo(2);
        assertThat(categoryRepository.count()).isEqualTo(4); // Salary, Salary::Base Salary, Food, Food::Groceries

        Account updatedAccount = accountRepository.findById(account.getId()).orElseThrow();
        // 0 + 500 - 50 = 450
        assertThat(updatedAccount.getBalance()).isEqualByComparingTo(new BigDecimal("450.00"));
    }
}
