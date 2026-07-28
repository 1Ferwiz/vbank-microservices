package com.ejada.vbank.transactionservice.exception;

public class TransactionAlreadyProcessedException extends RuntimeException {
    public TransactionAlreadyProcessedException(String message) {
        super(message);
    }
}