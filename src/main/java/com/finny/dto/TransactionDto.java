package com.finny.dto;

import com.finny.domain.enums.TransactionType;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class TransactionDto {
    private String id;
    private String accountId;
    private String accountName;
    private String toAccountId;
    private String toAccountName;
    private String categoryId;
    private String categoryName;
    private BigDecimal amount;
    private TransactionType type;
    private String description;
    private String tags;
    private LocalDateTime transactionDate;
    private LocalDateTime created;
    private LocalDateTime updated;
}
