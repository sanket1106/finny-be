package com.finny.repository;

import com.finny.BaseMySqlTest;
import com.finny.domain.Account;
import com.finny.domain.enums.AccountStatus;
import com.finny.domain.enums.AccountType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class AccountRepositoryTest extends BaseMySqlTest {

    @Autowired
    private AccountRepository accountRepository;

    @BeforeEach
    void setUp() {
        setupTenantContext("user_john_id", "family_smith_id");
    }

    @Test
    void testCreateAndFindAccount() {
        String accountName = "Test Account " + System.currentTimeMillis();
        Account account = new Account();
        account.setName(accountName);
        account.setType(AccountType.BANK);
        account.setCurrency("USD");
        account.setBalance(BigDecimal.valueOf(5000.00));
        account.setStatus(AccountStatus.ACTIVE);

        Account saved = accountRepository.save(account);
        assertThat(saved.getId()).isNotNull();

        Optional<Account> found = accountRepository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo(accountName);
        
        // Cleanup
        accountRepository.delete(saved);
    }
}
