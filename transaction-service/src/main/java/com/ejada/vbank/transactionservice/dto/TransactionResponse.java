package com.ejada.vbank.transactionservice.dto;

import com.ejada.vbank.transactionservice.entity.TransactionStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class TransactionResponse {
    private UUID transactionId;
    private UUID fromAccountId;
    private UUID toAccountId;
    private BigDecimal amount;
    private String description;
    private TransactionStatus status;
    private LocalDateTime timestamp;
}