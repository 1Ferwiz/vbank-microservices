package com.ejada.vbank.accountservice.controller;

import com.ejada.vbank.accountservice.dto.AccountResponse;
import com.ejada.vbank.accountservice.dto.CreateAccountRequest;
import com.ejada.vbank.accountservice.dto.CreateAccountResponse;
import com.ejada.vbank.accountservice.dto.TransferRequest;
import com.ejada.vbank.accountservice.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping("/accounts")
    public ResponseEntity<CreateAccountResponse> createAccount(
            @Valid @RequestBody CreateAccountRequest request) {
        CreateAccountResponse response = accountService.createAccount(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/accounts/{accountId}")
    public ResponseEntity<AccountResponse> getAccountById(@PathVariable UUID accountId) {
        return ResponseEntity.ok(accountService.getAccountById(accountId));
    }

    @GetMapping("/users/{userId}/accounts")
    public ResponseEntity<List<AccountResponse>> getAccountsByUserId(@PathVariable UUID userId) {
        return ResponseEntity.ok(accountService.getAccountsByUserId(userId));
    }

    @PutMapping("/accounts/transfer")
    public ResponseEntity<Void> transfer(@Valid @RequestBody TransferRequest request) {
        accountService.transfer(request);
        return ResponseEntity.ok().build();
    }
}