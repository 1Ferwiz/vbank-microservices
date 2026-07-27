package com.ejada.vbank.transactionservice.service;

import com.ejada.vbank.transactionservice.dto.CreateTransferRequest;
import com.ejada.vbank.transactionservice.dto.TransactionResponse;
import com.ejada.vbank.transactionservice.entity.Transaction;
import com.ejada.vbank.transactionservice.entity.TransactionStatus;
import com.ejada.vbank.transactionservice.exception.ResourceNotFoundException;
import com.ejada.vbank.transactionservice.exception.TransferFailedException;
import com.ejada.vbank.transactionservice.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountServiceClient accountServiceClient;

    public TransactionService(TransactionRepository transactionRepository,
                               AccountServiceClient accountServiceClient) {
        this.transactionRepository = transactionRepository;
        this.accountServiceClient = accountServiceClient;
    }

    public TransactionResponse transfer(CreateTransferRequest request) {
        if (request.getFromAccountId().equals(request.getToAccountId())) {
            throw new IllegalArgumentException("fromAccountId and toAccountId must be different.");
        }

        // 1. Record the transaction as INITIATED before touching Account Service,
        //    so there's always an audit trail even if the downstream call fails.
        Transaction transaction = new Transaction(
                request.getFromAccountId(),
                request.getToAccountId(),
                request.getAmount(),
                request.getDescription(),
                TransactionStatus.INITIATED
        );
        transaction = transactionRepository.saveAndFlush(transaction);

        // 2. Ask Account Service to actually move the money.
        try {
            accountServiceClient.executeTransfer(
                    request.getFromAccountId(),
                    request.getToAccountId(),
                    request.getAmount()
            );
        } catch (TransferFailedException ex) {
            transaction.setStatus(TransactionStatus.FAILED);
            transactionRepository.saveAndFlush(transaction);
            throw ex;
        }

        // 3. Mark SUCCESS only after Account Service confirms the balances moved.
        transaction.setStatus(TransactionStatus.SUCCESS);
        transaction = transactionRepository.saveAndFlush(transaction);

        return toTransactionResponse(transaction);
    }

    public TransactionResponse getTransactionById(UUID transactionId) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Transaction with ID " + transactionId + " not found."));

        return toTransactionResponse(transaction);
    }

    public List<TransactionResponse> getTransactionsByAccountId(UUID accountId) {
        List<Transaction> transactions =
                transactionRepository.findByFromAccountIdOrToAccountId(accountId, accountId);

        if (transactions.isEmpty()) {
            throw new ResourceNotFoundException(
                    "No transactions found for account ID " + accountId + ".");
        }

        return transactions.stream()
                .map(this::toTransactionResponse)
                .toList();
    }

    private TransactionResponse toTransactionResponse(Transaction transaction) {
        return new TransactionResponse(
                transaction.getId(),
                transaction.getFromAccountId(),
                transaction.getToAccountId(),
                transaction.getAmount(),
                transaction.getDescription(),
                transaction.getStatus(),
                transaction.getCreatedAt()
        );
    }
}
