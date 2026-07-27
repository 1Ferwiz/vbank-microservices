package com.ejada.vbank.accountservice.dto;

import com.ejada.vbank.accountservice.entity.AccountStatus;
import com.ejada.vbank.accountservice.entity.AccountType;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class AccountResponse {
    private UUID accountId;
    private String accountNumber;
    private AccountType accountType;
    private BigDecimal balance;
    private AccountStatus status;
}