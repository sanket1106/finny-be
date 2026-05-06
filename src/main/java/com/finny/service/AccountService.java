package com.finny.service;

import com.finny.domain.Account;
import com.finny.domain.enums.AccountStatus;
import com.finny.dto.CreateAccountRequest;
import com.finny.repository.AccountRepository;
import com.finny.repository.master.CurrencyCodeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final CurrencyCodeRepository currencyCodeRepository;

    public AccountService(AccountRepository accountRepository, CurrencyCodeRepository currencyCodeRepository) {
        this.accountRepository = accountRepository;
        this.currencyCodeRepository = currencyCodeRepository;
    }

    public String createAccount(CreateAccountRequest request) {
        // Validate against master DB (outside tenant transaction to avoid connection binding conflict)
        validateCurrencyCode(request.getCurrencyCode());

        // Execute tenant-specific logic in its own transaction
        return saveAccount(request);
    }

    @Transactional("masterTransactionManager")
    public void validateCurrencyCode(String currencyCode) {
        if (!currencyCodeRepository.existsById(currencyCode)) {
            throw new IllegalArgumentException("Invalid currency code: " + currencyCode);
        }
    }

    @Transactional("tenantTransactionManager")
    public String saveAccount(CreateAccountRequest request) {
        if (accountRepository.existsByName(request.getName())) {
            throw new IllegalArgumentException("Account with name '" + request.getName() + "' already exists");
        }

        Account account = new Account();
        account.setName(request.getName());
        account.setType(request.getType());
        account.setCurrency(request.getCurrencyCode());
        account.setBalance(request.getBalance());
        account.setStatus(AccountStatus.ACTIVE);

        Account savedAccount = accountRepository.save(account);
        return savedAccount.getId();
    }
}
