package com.ejada.vbank.bffservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponse {
    private UUID transactionId;
    private UUID toAccountId;
    private BigDecimal amount;
    private String description;
    private String status;
    private LocalDateTime timestamp;
}