package com.finny.service;

import com.finny.domain.Account;
import com.finny.domain.Category;
import com.finny.domain.Transaction;
import com.finny.domain.enums.TransactionType;
import com.finny.dto.CreateTransactionRequest;
import com.finny.dto.CreateTransactionResponse;
import com.finny.dto.PaginatedResponse;
import com.finny.dto.TransactionDto;
import com.finny.dto.TransactionFilterDto;
import com.finny.dto.UpdateTransactionRequest;
import com.finny.repository.AccountRepository;
import com.finny.repository.CategoryRepository;
import com.finny.repository.TransactionRepository;
import com.finny.repository.spec.TransactionSpecification;
import com.finny.config.UserContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.stream.Collectors;

import java.math.BigDecimal;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final CategoryRepository categoryRepository;

    public TransactionService(TransactionRepository transactionRepository,
            AccountRepository accountRepository,
            CategoryRepository categoryRepository) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
        this.categoryRepository = categoryRepository;
    }

    @Transactional("tenantTransactionManager")
    public void deleteTransaction(String id) {
        Transaction tx = transactionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found with ID: " + id));

        reverseBalances(tx);
        transactionRepository.delete(tx);
    }

    @Transactional("tenantTransactionManager")
    public void updateTransaction(String id, UpdateTransactionRequest request) {
        if (!id.equals(request.getId())) {
            throw new IllegalArgumentException("ID in path and body must match");
        }

        Transaction tx = transactionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found with ID: " + id));

        // 1. Reverse old impact
        reverseBalances(tx);

        // 2. Resolve new references
        Account account = resolveAccount(request.getAccountId());
        Category category = resolveCategory(request.getCategoryId());
        Account toAccount = null;
        if (request.getType() == TransactionType.TRANSFER) {
            if (request.getToAccountId() == null || request.getToAccountId().isBlank()) {
                throw new IllegalArgumentException("toAccountId is required for TRANSFER transactions");
            }
            toAccount = resolveAccount(request.getToAccountId());
        }

        // 3. Update transaction record
        tx.setAccount(account);
        tx.setToAccount(toAccount);
        tx.setCategory(category);
        tx.setAmount(request.getAmount());
        tx.setType(request.getType());
        tx.setDescription(request.getDescription());
        tx.setTags(request.getTags());
        tx.setTransactionDate(request.getTransactionDateTime());

        // 4. Apply new impact
        applyBalanceChange(account, toAccount, tx.getType(), tx.getAmount());

        // 5. Persist balance changes
        accountRepository.save(account);
        if (toAccount != null) {
            accountRepository.save(toAccount);
        }
        
        transactionRepository.save(tx);
    }

    private void reverseBalances(Transaction tx) {
        Account fromAccount = tx.getAccount();
        Account toAccount = tx.getToAccount();
        BigDecimal amount = tx.getAmount();

        switch (tx.getType()) {
            case INCOME -> debitAccount(fromAccount, amount);
            case SPENDING -> creditAccount(fromAccount, amount);
            case TRANSFER -> {
                creditAccount(fromAccount, amount);
                debitAccount(toAccount, amount);
            }
        }

        // Persist changes
        accountRepository.save(fromAccount);
        if (toAccount != null) {
            accountRepository.save(toAccount);
        }
    }

    @Transactional("tenantTransactionManager")
    public CreateTransactionResponse createTransaction(CreateTransactionRequest request) {
        // Validate and resolve references
        Account account = resolveAccount(request.getAccountId());
        Category category = resolveCategory(request.getCategoryId());

        // Transfer-specific validation
        Account toAccount = null;
        if (request.getType() == TransactionType.TRANSFER) {
            if (request.getToAccountId() == null || request.getToAccountId().isBlank()) {
                throw new IllegalArgumentException("toAccountId is required for TRANSFER transactions");
            }
            toAccount = resolveAccount(request.getToAccountId());
        }

        // Apply balance changes
        applyBalanceChange(account, toAccount, request.getType(), request.getAmount());

        // Persist balance changes
        accountRepository.save(account);
        if (toAccount != null) {
            accountRepository.save(toAccount);
        }

        // Build and persist transaction record
        Transaction transaction = buildTransaction(request, account, toAccount, category);
        Transaction saved = transactionRepository.save(transaction);

        return new CreateTransactionResponse(saved.getId());
    }

    @Transactional(value = "tenantTransactionManager", readOnly = true)
    public PaginatedResponse<TransactionDto> getTransactions(TransactionFilterDto filter, int page, int size) {
        String tenantId = UserContext.getTenantId();
        if (tenantId == null || tenantId.isBlank()) {
            throw new IllegalStateException("Tenant ID is missing from User Context.");
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "transactionDate"));

        Page<Transaction> transactionPage = transactionRepository.findAll(
                TransactionSpecification.withFilter(filter), pageable);

        return new PaginatedResponse<>(
                transactionPage.getContent().stream().map(this::mapToDto).collect(Collectors.toList()),
                transactionPage.getTotalElements(),
                transactionPage.getNumber(),
                transactionPage.getTotalPages());
    }

    private TransactionDto mapToDto(Transaction t) {
        TransactionDto dto = new TransactionDto();
        dto.setId(t.getId());
        if (t.getAccount() != null) {
            dto.setAccountId(t.getAccount().getId());
            dto.setAccountName(t.getAccount().getName());
        }
        if (t.getToAccount() != null) {
            dto.setToAccountId(t.getToAccount().getId());
            dto.setToAccountName(t.getToAccount().getName());
        }
        if (t.getCategory() != null) {
            dto.setCategoryId(t.getCategory().getId());
            dto.setCategoryName(t.getCategory().getName());
        }
        dto.setAmount(t.getAmount());
        dto.setType(t.getType());
        dto.setDescription(t.getDescription());
        dto.setTags(t.getTags());
        dto.setTransactionDate(t.getTransactionDate());
        dto.setCreated(t.getCreated());
        dto.setUpdated(t.getUpdated());
        return dto;
    }

    // --- Reusable helpers (designed for use in future update-transaction feature)
    // ---

    /**
     * Resolves an Account by ID, throws if not found.
     */
    public Account resolveAccount(String accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found: " + accountId));
    }

    /**
     * Resolves a Category by ID, throws if not found.
     */
    public Category resolveCategory(String categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Category not found: " + categoryId));
    }

    /**
     * Applies the balance delta to the source (and destination for TRANSFER)
     * accounts.
     * Can be reused directly in update-transaction to reverse + re-apply deltas.
     */
    public void applyBalanceChange(Account account, Account toAccount, TransactionType type, BigDecimal amount) {
        switch (type) {
            case INCOME -> creditAccount(account, amount);
            case SPENDING -> debitAccount(account, amount);
            case TRANSFER -> {
                debitAccount(account, amount);
                creditAccount(toAccount, amount);
            }
        }
    }

    /**
     * Adds amount to account balance.
     */
    public void creditAccount(Account account, BigDecimal amount) {
        account.setBalance(account.getBalance().add(amount));
    }

    /**
     * Subtracts amount from account balance. Balance may go negative.
     */
    public void debitAccount(Account account, BigDecimal amount) {
        account.setBalance(account.getBalance().subtract(amount));
    }

    /**
     * Builds a Transaction entity from a request and resolved references.
     */
    public Transaction buildTransaction(CreateTransactionRequest request,
            Account account,
            Account toAccount,
            Category category) {
        Transaction t = new Transaction();
        t.setAccount(account);
        t.setToAccount(toAccount);
        t.setCategory(category);
        t.setAmount(request.getAmount());
        t.setType(request.getType());
        t.setDescription(request.getDescription());
        t.setTags(request.getTags());
        t.setTransactionDate(request.getTransactionDateTime());
        return t;
    }
}
