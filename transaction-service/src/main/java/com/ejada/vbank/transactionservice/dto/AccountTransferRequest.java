package com.ejada.vbank.transactionservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Outgoing request body sent to Account Service's PUT /accounts/transfer.
 * Account Service is expected to debit fromAccountId and credit toAccountId
 * atomically and return 200 OK on success, or a 4xx/5xx with an ErrorResponse
 * body (e.g. insufficient funds, account not found/inactive) on failure.
 */
@Getter
@AllArgsConstructor
public class AccountTransferRequest {
    private UUID fromAccountId;
    private UUID toAccountId;
    private BigDecimal amount;
}
