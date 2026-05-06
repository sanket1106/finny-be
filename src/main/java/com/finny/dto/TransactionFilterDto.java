package com.finny.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class TransactionFilterDto {
    private String accountId;
    private String toAccountId;
    private LocalDateTime fromDate;
    private LocalDateTime toDate;
    private List<String> categoryIds;
}
