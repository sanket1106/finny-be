package com.finny.repository;

import com.finny.domain.Account;
import com.finny.domain.enums.AccountStatus;
import com.finny.domain.enums.AccountType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import com.finny.config.UserContext;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class AccountRepositoryTest {

    @Autowired
    private AccountRepository accountRepository;

    @BeforeEach
    void setUp() {
        UserContext.setCurrentUser("test_tenant");
    }

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    @Test
    void testSaveAndFindAccount() {
        Account account = new Account();
        account.setName("Test Account");
        account.setType(AccountType.BANK);
        account.setCurrency("USD");
        account.setBalance(BigDecimal.valueOf(1000.00));
        account.setStatus(AccountStatus.ACTIVE);

        Account savedAccount = accountRepository.save(account);

        assertThat(savedAccount.getId()).isNotNull();
        assertThat(savedAccount.getCreated()).isNotNull();

        Optional<Account> foundAccount = accountRepository.findById(savedAccount.getId());
        assertThat(foundAccount).isPresent();
        assertThat(foundAccount.get().getName()).isEqualTo("Test Account");
    }
}
