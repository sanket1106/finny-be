package com.finny.repository;

import com.finny.BaseMySqlTest;
import com.finny.domain.Account;
import com.finny.domain.Category;
import com.finny.domain.Transaction;
import com.finny.domain.enums.AccountStatus;
import com.finny.domain.enums.AccountType;
import com.finny.domain.enums.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class TransactionRepositoryTest extends BaseMySqlTest {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @BeforeEach
    void setUp() {
        setupTenantContext("user_john_id", "family_smith_id");
    }

    @Test
    void testCreateTransactionProgrammatically() {
        // 1. Create Account
        Account account = new Account();
        account.setName("TX Test Account");
        account.setType(AccountType.CASH);
        account.setCurrency("USD");
        account.setBalance(BigDecimal.valueOf(100.00));
        account.setStatus(AccountStatus.ACTIVE);
        account = accountRepository.save(account);

        // 2. Create Category
        Category category = new Category();
        category.setName("TX Test Category");
        category = categoryRepository.save(category);

        // 3. Create Transaction
        Transaction tx = new Transaction();
        tx.setAccount(account);
        tx.setCategory(category);
        tx.setAmount(BigDecimal.valueOf(25.50));
        tx.setType(TransactionType.SPENDING);
        tx.setTransactionDate(LocalDateTime.now());
        tx.setDescription("Programmatic TX");

        Transaction savedTx = transactionRepository.save(tx);
        assertThat(savedTx.getId()).isNotNull();

        Optional<Transaction> foundTx = transactionRepository.findById(savedTx.getId());
        assertThat(foundTx).isPresent();
        assertThat(foundTx.get().getDescription()).isEqualTo("Programmatic TX");

        // Cleanup
        transactionRepository.delete(savedTx);
        categoryRepository.delete(category);
        accountRepository.delete(account);
    }
}
