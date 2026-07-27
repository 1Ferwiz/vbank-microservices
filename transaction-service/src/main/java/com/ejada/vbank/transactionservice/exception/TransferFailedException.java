package com.ejada.vbank.transactionservice.exception;

/**
 * Thrown when Account Service rejects the debit/credit call (insufficient
 * funds, inactive/not-found account) or cannot be reached at all. The
 * transaction is still persisted with status FAILED before this is thrown.
 */
public class TransferFailedException extends RuntimeException {
    public TransferFailedException(String message) {
        super(message);
    }
}
