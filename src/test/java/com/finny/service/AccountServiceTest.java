package com.finny.service;

import com.finny.domain.Account;
import com.finny.domain.enums.AccountStatus;
import com.finny.domain.enums.AccountType;
import com.finny.dto.CreateAccountRequest;
import com.finny.repository.AccountRepository;
import com.finny.repository.master.CurrencyCodeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private CurrencyCodeRepository currencyCodeRepository;

    @InjectMocks
    private AccountService accountService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateAccount_Success() {
        CreateAccountRequest request = new CreateAccountRequest();
        request.setTenantId("tenant-123");
        request.setUserId("user-123");
        request.setName("Savings");
        request.setType(AccountType.BANK);
        request.setCurrencyCode("USD");
        request.setBalance(new BigDecimal("1000.00"));

        when(currencyCodeRepository.existsById("USD")).thenReturn(true);
        when(accountRepository.existsByName("Savings")).thenReturn(false);

        Account savedAccount = new Account();
        savedAccount.setId("acc-123");
        savedAccount.setStatus(AccountStatus.ACTIVE);
        when(accountRepository.save(any(Account.class))).thenReturn(savedAccount);

        String accountId = accountService.createAccount(request);

        assertEquals("acc-123", accountId);
        verify(accountRepository).save(argThat(acc -> 
            acc.getName().equals("Savings") && 
            acc.getStatus() == AccountStatus.ACTIVE &&
            acc.getCurrency().equals("USD")
        ));
    }

    @Test
    void testCreateAccount_InvalidCurrency() {
        CreateAccountRequest request = new CreateAccountRequest();
        request.setCurrencyCode("XYZ");

        when(currencyCodeRepository.existsById("XYZ")).thenReturn(false);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, 
            () -> accountService.createAccount(request));
        assertTrue(ex.getMessage().contains("Invalid currency code"));
    }

    @Test
    void testCreateAccount_DuplicateName() {
        CreateAccountRequest request = new CreateAccountRequest();
        request.setCurrencyCode("USD");
        request.setName("Savings");

        when(currencyCodeRepository.existsById("USD")).thenReturn(true);
        when(accountRepository.existsByName("Savings")).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, 
            () -> accountService.createAccount(request));
        assertTrue(ex.getMessage().contains("already exists"));
    }
}
