package com.ejada.vbank.transactionservice.dto;

import com.ejada.vbank.transactionservice.entity.TransactionStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class TransferResponse {
    private UUID transactionId;
    private TransactionStatus status;
    private LocalDateTime timestamp;
}