package com.ejada.vbank.transactionservice.service;

import com.ejada.vbank.transactionservice.dto.ExecuteTransferRequest;
import com.ejada.vbank.transactionservice.dto.InitiateTransferRequest;
import com.ejada.vbank.transactionservice.dto.TransactionResponse;
import com.ejada.vbank.transactionservice.dto.TransferResponse;
import com.ejada.vbank.transactionservice.entity.Transaction;
import com.ejada.vbank.transactionservice.entity.TransactionStatus;
import com.ejada.vbank.transactionservice.exception.ResourceNotFoundException;
import com.ejada.vbank.transactionservice.exception.TransactionAlreadyProcessedException;
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

    public TransferResponse initiateTransfer(InitiateTransferRequest request) {
        if (request.getFromAccountId().equals(request.getToAccountId())) {
            throw new IllegalArgumentException("fromAccountId and toAccountId must be different.");
        }

        Transaction transaction = new Transaction(
                request.getFromAccountId(),
                request.getToAccountId(),
                request.getAmount(),
                request.getDescription(),
                TransactionStatus.INITIATED
        );
        transaction = transactionRepository.saveAndFlush(transaction);

        return toTransferResponse(transaction);
    }

    public TransferResponse executeTransfer(ExecuteTransferRequest request) {
        Transaction transaction = transactionRepository.findById(request.getTransactionId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Transaction with ID " + request.getTransactionId() + " not found."));

        if (transaction.getStatus() != TransactionStatus.INITIATED) {
            throw new TransactionAlreadyProcessedException(
                    "Transaction " + transaction.getId() + " has already been processed (status: "
                            + transaction.getStatus() + ").");
        }

        try {
            accountServiceClient.executeTransfer(
                    transaction.getFromAccountId(),
                    transaction.getToAccountId(),
                    transaction.getAmount()
            );
        } catch (TransferFailedException ex) {
            transaction.setStatus(TransactionStatus.FAILED);
            transactionRepository.saveAndFlush(transaction);
            throw ex;
        }

        transaction.setStatus(TransactionStatus.SUCCESS);
        transaction = transactionRepository.saveAndFlush(transaction);

        return toTransferResponse(transaction);
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

    private TransferResponse toTransferResponse(Transaction transaction) {
        return new TransferResponse(
                transaction.getId(),
                transaction.getStatus(),
                transaction.getUpdatedAt()
        );
    }

    private TransactionResponse toTransactionResponse(Transaction transaction) {
        return new TransactionResponse(
                transaction.getId(),
                transaction.getFromAccountId(),
                transaction.getToAccountId(),
                transaction.getAmount(),
                transaction.getDescription(),
                transaction.getCreatedAt()
        );
    }
}