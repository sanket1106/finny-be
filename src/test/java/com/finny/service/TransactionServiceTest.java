package com.finny.service;

import com.finny.domain.Account;
import com.finny.domain.Category;
import com.finny.domain.Transaction;
import com.finny.domain.enums.TransactionType;
import com.finny.dto.CreateTransactionRequest;
import com.finny.dto.CreateTransactionResponse;
import com.finny.dto.UpdateTransactionRequest;
import com.finny.repository.AccountRepository;
import com.finny.repository.CategoryRepository;
import com.finny.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private TransactionService transactionService;

    private Account sourceAccount;
    private Account targetAccount;
    private Category category;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        sourceAccount = new Account();
        sourceAccount.setId("src-acc");
        sourceAccount.setBalance(new BigDecimal("1000.00"));

        targetAccount = new Account();
        targetAccount.setId("target-acc");
        targetAccount.setBalance(new BigDecimal("500.00"));

        category = new Category();
        category.setId("cat-1");
    }

    @Test
    void testCreateTransaction_Income() {
        CreateTransactionRequest request = new CreateTransactionRequest();
        request.setAccountId("src-acc");
        request.setCategoryId("cat-1");
        request.setAmount(new BigDecimal("200.00"));
        request.setType(TransactionType.INCOME);
        request.setTransactionDateTime(LocalDateTime.now());

        when(accountRepository.findById("src-acc")).thenReturn(Optional.of(sourceAccount));
        when(categoryRepository.findById("cat-1")).thenReturn(Optional.of(category));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(i -> i.getArguments()[0]);

        CreateTransactionResponse response = transactionService.createTransaction(request);

        assertNotNull(response);
        assertEquals(new BigDecimal("1200.00"), sourceAccount.getBalance());
        verify(accountRepository).save(sourceAccount);
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void testCreateTransaction_Spending() {
        CreateTransactionRequest request = new CreateTransactionRequest();
        request.setAccountId("src-acc");
        request.setCategoryId("cat-1");
        request.setAmount(new BigDecimal("200.00"));
        request.setType(TransactionType.SPENDING);
        request.setTransactionDateTime(LocalDateTime.now());

        when(accountRepository.findById("src-acc")).thenReturn(Optional.of(sourceAccount));
        when(categoryRepository.findById("cat-1")).thenReturn(Optional.of(category));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(i -> i.getArguments()[0]);

        transactionService.createTransaction(request);

        assertEquals(new BigDecimal("800.00"), sourceAccount.getBalance());
        verify(accountRepository).save(sourceAccount);
    }

    @Test
    void testCreateTransaction_Transfer_Success() {
        CreateTransactionRequest request = new CreateTransactionRequest();
        request.setAccountId("src-acc");
        request.setToAccountId("target-acc");
        request.setCategoryId("cat-1");
        request.setAmount(new BigDecimal("100.00"));
        request.setType(TransactionType.TRANSFER);
        request.setTransactionDateTime(LocalDateTime.now());

        when(accountRepository.findById("src-acc")).thenReturn(Optional.of(sourceAccount));
        when(accountRepository.findById("target-acc")).thenReturn(Optional.of(targetAccount));
        when(categoryRepository.findById("cat-1")).thenReturn(Optional.of(category));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(i -> i.getArguments()[0]);

        transactionService.createTransaction(request);

        assertEquals(new BigDecimal("900.00"), sourceAccount.getBalance());
        assertEquals(new BigDecimal("600.00"), targetAccount.getBalance());
        
        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository).save(captor.capture());
        Transaction saved = captor.getValue();
        assertEquals(sourceAccount, saved.getAccount());
        assertEquals(targetAccount, saved.getToAccount());
    }

    @Test
    void testCreateTransaction_Transfer_MissingToAccount_ThrowsException() {
        CreateTransactionRequest request = new CreateTransactionRequest();
        request.setType(TransactionType.TRANSFER);
        request.setAccountId("src-acc");
        request.setCategoryId("cat-1");

        when(accountRepository.findById("src-acc")).thenReturn(Optional.of(sourceAccount));
        when(categoryRepository.findById("cat-1")).thenReturn(Optional.of(category));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, 
                () -> transactionService.createTransaction(request));
        assertEquals("toAccountId is required for TRANSFER transactions", ex.getMessage());
    }

    @Test
    void testCreateTransaction_AccountNotFound_ThrowsException() {
        CreateTransactionRequest request = new CreateTransactionRequest();
        request.setAccountId("invalid-acc");

        when(accountRepository.findById("invalid-acc")).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, 
                () -> transactionService.createTransaction(request));
        assertTrue(ex.getMessage().contains("Account not found"));
    }

    @Test
    void testCreateTransaction_CategoryNotFound_ThrowsException() {
        CreateTransactionRequest request = new CreateTransactionRequest();
        request.setAccountId("src-acc");
        request.setCategoryId("invalid-cat");

        when(accountRepository.findById("src-acc")).thenReturn(Optional.of(sourceAccount));
        when(categoryRepository.findById("invalid-cat")).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, 
                () -> transactionService.createTransaction(request));
        assertTrue(ex.getMessage().contains("Category not found"));
    }

    @Test
    void testDeleteTransaction_Income_ReversesBalance() {
        Transaction tx = new Transaction();
        tx.setId("tx-1");
        tx.setType(TransactionType.INCOME);
        tx.setAccount(sourceAccount);
        tx.setAmount(new BigDecimal("200.00"));

        when(transactionRepository.findById("tx-1")).thenReturn(Optional.of(tx));

        transactionService.deleteTransaction("tx-1");

        // 1000 - 200 = 800
        assertEquals(new BigDecimal("800.00"), sourceAccount.getBalance());
        verify(transactionRepository).delete(tx);
        verify(accountRepository).save(sourceAccount);
    }

    @Test
    void testDeleteTransaction_Spending_ReversesBalance() {
        Transaction tx = new Transaction();
        tx.setId("tx-1");
        tx.setType(TransactionType.SPENDING);
        tx.setAccount(sourceAccount);
        tx.setAmount(new BigDecimal("150.00"));

        when(transactionRepository.findById("tx-1")).thenReturn(Optional.of(tx));

        transactionService.deleteTransaction("tx-1");

        // 1000 + 150 = 1150
        assertEquals(new BigDecimal("1150.00"), sourceAccount.getBalance());
        verify(transactionRepository).delete(tx);
        verify(accountRepository).save(sourceAccount);
    }

    @Test
    void testDeleteTransaction_Transfer_ReversesBothBalances() {
        Transaction tx = new Transaction();
        tx.setId("tx-1");
        tx.setType(TransactionType.TRANSFER);
        tx.setAccount(sourceAccount);
        tx.setToAccount(targetAccount);
        tx.setAmount(new BigDecimal("300.00"));

        when(transactionRepository.findById("tx-1")).thenReturn(Optional.of(tx));

        transactionService.deleteTransaction("tx-1");

        // Source: 1000 + 300 = 1300
        // Target: 500 - 300 = 200
        assertEquals(new BigDecimal("1300.00"), sourceAccount.getBalance());
        assertEquals(new BigDecimal("200.00"), targetAccount.getBalance());
        verify(transactionRepository).delete(tx);
        verify(accountRepository).save(sourceAccount);
        verify(accountRepository).save(targetAccount);
    }

    @Test
    void testDeleteTransaction_NotFound_ThrowsException() {
        when(transactionRepository.findById("tx-none")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, 
                () -> transactionService.deleteTransaction("tx-none"));
    }

    @Test
    void testUpdateTransaction_ChangeAmount_RecalculatesBalances() {
        // Setup existing transaction (Income 200)
        Transaction existingTx = new Transaction();
        existingTx.setId("tx-update");
        existingTx.setType(TransactionType.INCOME);
        existingTx.setAccount(sourceAccount);
        existingTx.setAmount(new BigDecimal("200.00"));
        existingTx.setCategory(category);

        when(transactionRepository.findById("tx-update")).thenReturn(Optional.of(existingTx));
        when(accountRepository.findById("src-acc")).thenReturn(Optional.of(sourceAccount));
        when(categoryRepository.findById("cat-1")).thenReturn(Optional.of(category));

        // New request: Spending 100
        UpdateTransactionRequest request = new UpdateTransactionRequest();
        request.setId("tx-update");
        request.setAccountId("src-acc");
        request.setCategoryId("cat-1");
        request.setAmount(new BigDecimal("100.00"));
        request.setType(TransactionType.SPENDING);
        request.setTransactionDateTime(LocalDateTime.now());

        transactionService.updateTransaction("tx-update", request);

        // Logic:
        // 1. Reverse Income 200: 1000 - 200 = 800
        // 2. Apply Spending 100: 800 - 100 = 700
        assertEquals(new BigDecimal("700.00"), sourceAccount.getBalance());
        assertEquals(TransactionType.SPENDING, existingTx.getType());
        assertEquals(new BigDecimal("100.00"), existingTx.getAmount());
        verify(transactionRepository).save(existingTx);
    }
}
