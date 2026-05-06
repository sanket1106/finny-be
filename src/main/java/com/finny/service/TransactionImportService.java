package com.finny.service;

import com.finny.config.UserContext;
import com.finny.domain.Account;
import com.finny.domain.Category;
import com.finny.domain.Transaction;
import com.finny.domain.enums.TransactionType;
import com.finny.dto.ImportResponseDto;
import com.finny.repository.AccountRepository;
import com.finny.repository.CategoryRepository;
import com.finny.repository.TransactionRepository;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

@Service
public class TransactionImportService {

    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;
    private final AccountRepository accountRepository;

    private static final String[] EXPECTED_HEADERS = {
            "transaction date", "amount", "transaction type", "account name", "account id",
            "to account name", "to account id", "category name", "subcategory name", "description"
    };

    public TransactionImportService(TransactionRepository transactionRepository,
                                    CategoryRepository categoryRepository,
                                    AccountRepository accountRepository) {
        this.transactionRepository = transactionRepository;
        this.categoryRepository = categoryRepository;
        this.accountRepository = accountRepository;
    }

    @Transactional("tenantTransactionManager")
    public ImportResponseDto importCsv(MultipartFile file) {
        List<String> errors = new ArrayList<>();
        int successfulRows = 0;
        int failedRows = 0;
        int totalRows = 0;

        try (CSVReader reader = new CSVReader(new InputStreamReader(file.getInputStream()))) {
            String[] headers = reader.readNext();
            if (headers == null || !validateHeaders(headers)) {
                errors.add("Row 1: Invalid CSV headers. Expected exactly: " + String.join(", ", EXPECTED_HEADERS));
                return new ImportResponseDto(totalRows, successfulRows, failedRows, errors);
            }

            List<String[]> rows = new ArrayList<>();
            String[] line;
            while ((line = reader.readNext()) != null) {
                rows.add(line);
                totalRows++;
            }

            // Validation Pass 1: Ensure all referenced accounts exist
            Set<String> requiredAccountIds = new HashSet<>();
            Set<String> requiredAccountNames = new HashSet<>();

            for (int i = 0; i < rows.size(); i++) {
                String[] row = rows.get(i);
                if (row.length < 10) continue;

                String accountName = row[3].trim();
                String accountId = row[4].trim();
                String toAccountName = row[5].trim();
                String toAccountId = row[6].trim();

                if (!accountId.isEmpty()) requiredAccountIds.add(accountId);
                else if (!accountName.isEmpty()) requiredAccountNames.add(accountName);

                if (!toAccountId.isEmpty()) requiredAccountIds.add(toAccountId);
                else if (!toAccountName.isEmpty()) requiredAccountNames.add(toAccountName);
            }

            // Fetch accounts from DB
            List<Account> allAccounts = accountRepository.findAll();
            Map<String, Account> accountById = new HashMap<>();
            Map<String, Account> accountByName = new HashMap<>();

            for (Account acc : allAccounts) {
                accountById.put(acc.getId(), acc);
                accountByName.put(acc.getName(), acc);
            }

            List<String> missingAccounts = new ArrayList<>();
            for (String id : requiredAccountIds) {
                if (!accountById.containsKey(id)) {
                    missingAccounts.add("ID: " + id);
                }
            }
            for (String name : requiredAccountNames) {
                if (!accountByName.containsKey(name)) {
                    missingAccounts.add("Name: " + name);
                }
            }

            if (!missingAccounts.isEmpty()) {
                errors.add("Import aborted. The following accounts are missing: " + String.join(", ", missingAccounts));
                return new ImportResponseDto(totalRows, successfulRows, failedRows, errors);
            }

            // Delete existing transactions and categories
            transactionRepository.deleteAll();
            categoryRepository.deleteAll();

            // Clear balances for existing accounts to start fresh
            for (Account acc : allAccounts) {
                acc.setBalance(BigDecimal.ZERO);
            }

            String tenantId = UserContext.getTenantId();

            // Process Rows
            Map<String, Category> createdCategories = new HashMap<>();

            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

            for (int i = 0; i < rows.size(); i++) {
                String[] row = rows.get(i);
                int rowNum = i + 2; // +1 for 0-index, +1 for header

                if (row.length < 10) {
                    errors.add("Row " + rowNum + ": Incorrect number of columns.");
                    failedRows++;
                    continue;
                }

                String dateStr = row[0].trim();
                String amountStr = row[1].trim();
                String typeStr = row[2].trim();
                String accountName = row[3].trim();
                String accountId = row[4].trim();
                String toAccountName = row[5].trim();
                String toAccountId = row[6].trim();
                String categoryName = row[7].trim();
                String subcategoryName = row[8].trim();
                String description = row[9].trim();

                // Validations
                LocalDateTime transactionDate;
                try {
                    transactionDate = LocalDate.parse(dateStr, dateFormatter).atStartOfDay();
                } catch (DateTimeParseException e) {
                    errors.add("Row " + rowNum + ": Invalid date format. Expected DD-MM-YYYY.");
                    failedRows++;
                    continue;
                }

                BigDecimal amount;
                try {
                    amount = new BigDecimal(amountStr);
                    if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                        errors.add("Row " + rowNum + ": Amount must be greater than zero.");
                        failedRows++;
                        continue;
                    }
                } catch (NumberFormatException e) {
                    errors.add("Row " + rowNum + ": Invalid amount format.");
                    failedRows++;
                    continue;
                }

                TransactionType type;
                try {
                    type = TransactionType.valueOf(typeStr.toUpperCase());
                } catch (IllegalArgumentException e) {
                    errors.add("Row " + rowNum + ": Invalid transaction type.");
                    failedRows++;
                    continue;
                }

                Account fromAccount = getAccount(accountId, accountName, accountById, accountByName);
                if (fromAccount == null) {
                    errors.add("Row " + rowNum + ": Source account is required but not found.");
                    failedRows++;
                    continue;
                }

                Account toAccount = null;
                if (type == TransactionType.TRANSFER) {
                    toAccount = getAccount(toAccountId, toAccountName, accountById, accountByName);
                    if (toAccount == null) {
                        errors.add("Row " + rowNum + ": 'to account' is required for TRANSFER type.");
                        failedRows++;
                        continue;
                    }
                }

                // Category processing
                Category category = null;
                if (!categoryName.isEmpty()) {
                    String catKey = categoryName.toLowerCase();
                    category = createdCategories.get(catKey);
                    if (category == null) {
                        category = new Category();
                        category.setName(categoryName);
                        category = categoryRepository.save(category);
                        createdCategories.put(catKey, category);
                    }

                    if (!subcategoryName.isEmpty()) {
                        String subcatKey = catKey + "::" + subcategoryName.toLowerCase();
                        Category subcategory = createdCategories.get(subcatKey);
                        if (subcategory == null) {
                            subcategory = new Category();
                            subcategory.setName(subcategoryName);
                            subcategory.setParent(category);
                            subcategory = categoryRepository.save(subcategory);
                            createdCategories.put(subcatKey, subcategory);
                        }
                        category = subcategory; // link transaction to subcategory
                    }
                }

                // Create Transaction
                Transaction tx = new Transaction();
                tx.setTransactionDate(transactionDate);
                tx.setAmount(amount);
                tx.setType(type);
                tx.setAccount(fromAccount);
                if (type == TransactionType.TRANSFER) {
                    tx.setToAccount(toAccount);
                }
                tx.setCategory(category);
                tx.setDescription(description.isEmpty() ? null : description);

                transactionRepository.save(tx);

                // Update Balances
                if (type == TransactionType.INCOME) {
                    fromAccount.setBalance(fromAccount.getBalance().add(amount));
                } else if (type == TransactionType.SPENDING) {
                    fromAccount.setBalance(fromAccount.getBalance().subtract(amount));
                } else if (type == TransactionType.TRANSFER) {
                    fromAccount.setBalance(fromAccount.getBalance().subtract(amount));
                    toAccount.setBalance(toAccount.getBalance().add(amount));
                }

                successfulRows++;
            }

            // Save updated balances
            accountRepository.saveAll(allAccounts);

        } catch (Exception e) {
            errors.add("An unexpected error occurred during import: " + e.getMessage());
            e.printStackTrace();
        }

        return new ImportResponseDto(totalRows, successfulRows, failedRows, errors);
    }

    private boolean validateHeaders(String[] headers) {
        if (headers.length < EXPECTED_HEADERS.length) return false;
        for (int i = 0; i < EXPECTED_HEADERS.length; i++) {
            if (!EXPECTED_HEADERS[i].equalsIgnoreCase(headers[i].trim())) {
                return false;
            }
        }
        return true;
    }

    private Account getAccount(String id, String name, Map<String, Account> byId, Map<String, Account> byName) {
        if (id != null && !id.isEmpty() && byId.containsKey(id)) {
            return byId.get(id);
        }
        if (name != null && !name.isEmpty() && byName.containsKey(name)) {
            return byName.get(name);
        }
        return null;
    }
}
