package com.finny.service;

import com.finny.config.UserContext;
import com.finny.domain.Account;
import com.finny.domain.enums.TransactionType;
import com.finny.dto.ImportResponseDto;
import com.finny.repository.AccountRepository;
import com.finny.repository.CategoryRepository;
import com.finny.repository.TransactionRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionImportServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private TransactionImportService transactionImportService;

    @BeforeEach
    void setUp() {
        UserContext.setCurrentUser("test-user-id");
        UserContext.setTenantId("test-tenant-id");
    }

    @AfterEach
    void cleanUp() {
        UserContext.clear();
    }

    @Test
    void importCsv_WithValidData_ReturnsSuccess() throws Exception {
        // Arrange
        String csvContent = "transaction date,amount,transaction type,account name,account id,to account name,to account id,category name,subcategory name,description\n" +
                "15-10-2023,100.50,INCOME,MyBank,acc-1,,,Salary,,October Salary\n" +
                "16-10-2023,20.00,SPENDING,MyBank,acc-1,,,Food,Groceries,Supermarket";

        MockMultipartFile file = new MockMultipartFile("file", "transactions.csv", "text/csv", csvContent.getBytes(StandardCharsets.UTF_8));

        Account acc = new Account();
        acc.setId("acc-1");
        acc.setName("MyBank");
        acc.setBalance(new BigDecimal("500.00"));

        when(accountRepository.findAll()).thenReturn(List.of(acc));

        // Act
        ImportResponseDto response = transactionImportService.importCsv(file);

        // Assert
        assertThat(response.getErrors()).isEmpty();
        assertThat(response.getTotalRows()).isEqualTo(2);
        assertThat(response.getSuccessfulRows()).isEqualTo(2);
        assertThat(response.getFailedRows()).isEqualTo(0);

        verify(transactionRepository, times(1)).deleteAll();
        verify(categoryRepository, times(1)).deleteAll();
        verify(transactionRepository, times(2)).save(any());
        verify(accountRepository, times(1)).saveAll(any());

        // Initial 500 -> cleared to 0, then Income 100.50 -> Spending 20.00 => 80.50
        assertThat(acc.getBalance()).isEqualByComparingTo(new BigDecimal("80.50"));
    }

    @Test
    void importCsv_WithMissingAccount_AbortsImport() throws Exception {
        // Arrange
        String csvContent = "transaction date,amount,transaction type,account name,account id,to account name,to account id,category name,subcategory name,description\n" +
                "15-10-2023,100.50,INCOME,MyBank,missing-acc-id,,,Salary,,October Salary\n";

        MockMultipartFile file = new MockMultipartFile("file", "transactions.csv", "text/csv", csvContent.getBytes(StandardCharsets.UTF_8));

        when(accountRepository.findAll()).thenReturn(List.of());

        // Act
        ImportResponseDto response = transactionImportService.importCsv(file);

        // Assert
        assertThat(response.getErrors()).hasSize(1);
        assertThat(response.getErrors().get(0)).contains("Import aborted");
        assertThat(response.getErrors().get(0)).contains("missing-acc-id");
        
        // Ensure no deletion occurred
        verify(transactionRepository, never()).deleteAll();
    }

    @Test
    void importCsv_WithInvalidHeader_ReturnsError() throws Exception {
        // Arrange
        String csvContent = "invalid header 1, invalid header 2\n" +
                "15-10-2023,100.50\n";

        MockMultipartFile file = new MockMultipartFile("file", "transactions.csv", "text/csv", csvContent.getBytes(StandardCharsets.UTF_8));

        // Act
        ImportResponseDto response = transactionImportService.importCsv(file);

        // Assert
        assertThat(response.getErrors()).hasSize(1);
        assertThat(response.getErrors().get(0)).contains("Invalid CSV headers");
        
        verify(accountRepository, never()).findAll();
    }
}
