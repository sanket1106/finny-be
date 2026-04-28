package com.finny.repository;

import com.finny.domain.Account;
import com.finny.domain.Category;
import com.finny.domain.Transaction;
import com.finny.domain.enums.AccountStatus;
import com.finny.domain.enums.AccountType;

import com.finny.domain.enums.TransactionType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class TransactionRepositoryTest {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    void testSaveAndFindTransaction() {
        // Setup Account
        Account account = new Account();
        account.setName("Test Account");
        account.setType(AccountType.CREDIT);
        account.setCurrency("USD");
        account.setBalance(BigDecimal.ZERO);
        account.setStatus(AccountStatus.ACTIVE);
        account = accountRepository.save(account);

        // Setup Category
        Category category = new Category();
        category.setName("Food");
        category = categoryRepository.save(category);

        // Setup Transaction
        Transaction transaction = new Transaction();
        transaction.setAccount(account);
        transaction.setCategory(category);
        transaction.setAmount(BigDecimal.valueOf(50.00));
        transaction.setType(TransactionType.SPENDING);
        transaction.setTransactionDate(LocalDateTime.now());
        transaction.setDescription("Lunch");

        Transaction savedTransaction = transactionRepository.save(transaction);

        assertThat(savedTransaction.getId()).isNotNull();

        Optional<Transaction> foundTransaction = transactionRepository.findById(savedTransaction.getId());
        assertThat(foundTransaction).isPresent();
        assertThat(foundTransaction.get().getAmount()).isEqualByComparingTo(BigDecimal.valueOf(50.00));
        assertThat(foundTransaction.get().getAccount().getId()).isEqualTo(account.getId());
    }
}
