package com.ejada.vbank.transactionservice.controller;

import com.ejada.vbank.transactionservice.dto.ExecuteTransferRequest;
import com.ejada.vbank.transactionservice.dto.InitiateTransferRequest;
import com.ejada.vbank.transactionservice.dto.TransactionResponse;
import com.ejada.vbank.transactionservice.dto.TransferResponse;
import com.ejada.vbank.transactionservice.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping("/transactions/transfer/initiation")
    public ResponseEntity<TransferResponse> initiateTransfer(
            @Valid @RequestBody InitiateTransferRequest request) {
        return ResponseEntity.ok(transactionService.initiateTransfer(request));
    }

    @PostMapping("/transactions/transfer/execution")
    public ResponseEntity<TransferResponse> executeTransfer(
            @Valid @RequestBody ExecuteTransferRequest request) {
        return ResponseEntity.ok(transactionService.executeTransfer(request));
    }

    @GetMapping("/accounts/{accountId}/transactions")
    public ResponseEntity<List<TransactionResponse>> getTransactionsByAccountId(
            @PathVariable UUID accountId) {
        return ResponseEntity.ok(transactionService.getTransactionsByAccountId(accountId));
    }
}