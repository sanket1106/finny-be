package com.finny.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateTransactionRequest extends CreateTransactionRequest {

    @NotBlank(message = "Transaction ID is required")
    private String id;
}
