package com.ejada.vbank.transactionservice.entity;

import com.fasterxml.jackson.annotation.JsonValue;

public enum TransactionStatus {
    INITIATED("Initiated"),
    SUCCESS("Success"),
    FAILED("Failed");

    private final String displayValue;

    TransactionStatus(String displayValue) {
        this.displayValue = displayValue;
    }

    @JsonValue
    public String getDisplayValue() {
        return displayValue;
    }
}