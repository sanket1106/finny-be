package com.finny.controller;

import com.finny.dto.CreateAccountRequest;
import com.finny.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/accounts")
@Tag(name = "Accounts", description = "Manage accounts within a tenant. All endpoints require a valid Bearer token.")
@SecurityRequirement(name = "bearerAuth")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping
    @Operation(
        summary = "Create a new account",
        description = "Creates a new financial account (bank, credit, cash, etc.) in the caller's tenant database. The currency code must be a valid code from the master list.",
        responses = {
            @ApiResponse(responseCode = "201", description = "Account created successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error, duplicate name, or invalid currency"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid Authorization token")
        }
    )
    public ResponseEntity<?> createAccount(@Valid @RequestBody CreateAccountRequest request) {
        try {
            String accountId = accountService.createAccount(request);
            return ResponseEntity.status(HttpStatus.CREATED).body("Account created successfully with ID: " + accountId);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to create account: " + e.getMessage());
        }
    }
}
