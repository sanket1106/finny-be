package com.finny.dto;

import com.finny.domain.enums.TransactionType;
import com.finny.validation.ValidAmount;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class CreateTransactionRequest {

    @NotBlank(message = "Account ID is required")
    private String accountId;

    // Required only for TRANSFER, validated in service
    private String toAccountId;

    @NotBlank(message = "Category ID is required")
    private String categoryId;

    @NotNull(message = "Amount is required")
    @ValidAmount
    private BigDecimal amount;

    @NotNull(message = "Transaction type is required")
    private TransactionType type;

    // Optional
    private String description;

    // Optional — comma-separated or JSON array string
    private String tags;

    @NotNull(message = "Transaction date is required")
    private LocalDateTime transactionDateTime;
}
