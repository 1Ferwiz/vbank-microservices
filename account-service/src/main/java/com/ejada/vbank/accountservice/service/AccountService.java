package com.ejada.vbank.accountservice.service;

import com.ejada.vbank.accountservice.dto.AccountResponse;
import com.ejada.vbank.accountservice.dto.CreateAccountRequest;
import com.ejada.vbank.accountservice.dto.CreateAccountResponse;
import com.ejada.vbank.accountservice.entity.Account;
import com.ejada.vbank.accountservice.entity.AccountStatus;
import com.ejada.vbank.accountservice.exception.ResourceNotFoundException;
import com.ejada.vbank.accountservice.repository.AccountRepository;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class AccountService {

    private static final int ACCOUNT_NUMBER_LENGTH = 10;
    private static final int MAX_GENERATION_ATTEMPTS = 5;

    private final AccountRepository accountRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public CreateAccountResponse createAccount(CreateAccountRequest request) {
        String accountNumber = generateUniqueAccountNumber();

        Account account = new Account(
                request.getUserId(),
                accountNumber,
                request.getAccountType(),
                request.getInitialBalance(),
                AccountStatus.ACTIVE,
                LocalDateTime.now()
        );

        Account saved = accountRepository.saveAndFlush(account);

        return new CreateAccountResponse(
                saved.getId(),
                saved.getAccountNumber(),
                "Account created successfully."
        );
    }

    public AccountResponse getAccountById(UUID accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Account with ID " + accountId + " not found."));

        return toAccountResponse(account);
    }

    public List<AccountResponse> getAccountsByUserId(UUID userId) {
        List<Account> accounts = accountRepository.findByUserId(userId);

        if (accounts.isEmpty()) {
            throw new ResourceNotFoundException(
                    "No accounts found for user ID " + userId + ".");
        }

        return accounts.stream()
                .map(this::toAccountResponse)
                .toList();
    }

    private String generateUniqueAccountNumber() {
        for (int attempt = 0; attempt < MAX_GENERATION_ATTEMPTS; attempt++) {
            String candidate = generateRandomAccountNumber();
            if (!accountRepository.existsByAccountNumber(candidate)) {
                return candidate;
            }
        }
        throw new IllegalStateException(
                "Failed to generate a unique account number after " + MAX_GENERATION_ATTEMPTS + " attempts.");
    }

    private String generateRandomAccountNumber() {
        StringBuilder sb = new StringBuilder(ACCOUNT_NUMBER_LENGTH);
        for (int i = 0; i < ACCOUNT_NUMBER_LENGTH; i++) {
            sb.append(secureRandom.nextInt(10));
        }
        return sb.toString();
    }

    private AccountResponse toAccountResponse(Account account) {
        return new AccountResponse(
                account.getId(),
                account.getAccountNumber(),
                account.getAccountType(),
                account.getBalance(),
                account.getStatus()
        );
    }
}