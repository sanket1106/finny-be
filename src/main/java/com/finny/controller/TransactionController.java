package com.finny.controller;

import com.finny.dto.CreateTransactionRequest;
import com.finny.dto.CreateTransactionResponse;
import com.finny.dto.UpdateTransactionRequest;
import com.finny.service.TransactionService;
import com.finny.service.TransactionImportService;
import com.finny.dto.ImportResponseDto;
import org.springframework.web.multipart.MultipartFile;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.finny.dto.PaginatedResponse;
import com.finny.dto.TransactionDto;
import com.finny.dto.TransactionFilterDto;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/transactions")
@Tag(name = "Transactions", description = "Manage transactions within a tenant.")
@SecurityRequirement(name = "bearerAuth")
public class TransactionController {

    private final TransactionService transactionService;
    private final TransactionImportService transactionImportService;

    public TransactionController(TransactionService transactionService,
            TransactionImportService transactionImportService) {
        this.transactionService = transactionService;
        this.transactionImportService = transactionImportService;
    }

    @PostMapping
    @Operation(summary = "Create a new transaction", description = "Creates a new transaction (Income, Spending, or Transfer) and updates account balances accordingly.", responses = {
            @ApiResponse(responseCode = "201", description = "Transaction created successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error or business logic violation"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid Authorization token")
    })
    public ResponseEntity<?> createTransaction(@Valid @RequestBody CreateTransactionRequest request) {
        try {
            CreateTransactionResponse response = transactionService.createTransaction(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to create transaction: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing transaction", description = "Updates an existing transaction, reversing the old balance impact and applying the new one.", responses = {
            @ApiResponse(responseCode = "200", description = "Transaction updated successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error or business logic violation"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid Authorization token")
    })
    public ResponseEntity<?> updateTransaction(@PathVariable String id, @Valid @RequestBody UpdateTransactionRequest request) {
        try {
            transactionService.updateTransaction(id, request);
            return ResponseEntity.ok("Transaction updated successfully");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to update transaction: " + e.getMessage());
        }
    }

    @GetMapping
    @Operation(summary = "Fetch transactions", description = "Fetch a paginated list of transactions with optional filters.", responses = {
            @ApiResponse(responseCode = "200", description = "Transactions retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid Authorization token")
    })
    public ResponseEntity<PaginatedResponse<TransactionDto>> getTransactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String accountId,
            @RequestParam(required = false) String toAccountId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate,
            @RequestParam(required = false) List<String> categoryIds) {

        TransactionFilterDto filter = new TransactionFilterDto();
        filter.setAccountId(accountId);
        filter.setToAccountId(toAccountId);
        filter.setFromDate(fromDate);
        filter.setToDate(toDate);
        filter.setCategoryIds(categoryIds);

        PaginatedResponse<TransactionDto> response = transactionService.getTransactions(filter, page, size);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Import transactions via CSV", description = "Import bulk transactions. Note: This will delete existing transactions and categories before importing.")
    @PostMapping(value = "/import", consumes = { "multipart/form-data" })
    public ResponseEntity<ImportResponseDto> importTransactions(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            ImportResponseDto errorResp = new ImportResponseDto();
            errorResp.setErrors(List.of("File is empty."));
            return ResponseEntity.badRequest().body(errorResp);
        }
        ImportResponseDto response = transactionImportService.importCsv(file);
        if (!response.getErrors().isEmpty() && response.getErrors().get(0).contains("Import aborted")) {
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a transaction", description = "Deletes an existing transaction and reverses the balance impact on the associated account(s).", responses = {
            @ApiResponse(responseCode = "204", description = "Transaction deleted successfully"),
            @ApiResponse(responseCode = "400", description = "Transaction not found or business logic violation"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid Authorization token")
    })
    public ResponseEntity<?> deleteTransaction(@PathVariable String id) {
        try {
            transactionService.deleteTransaction(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to delete transaction: " + e.getMessage());
        }
    }
}
