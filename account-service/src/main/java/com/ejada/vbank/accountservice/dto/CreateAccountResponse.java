package com.ejada.vbank.accountservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class CreateAccountResponse {
    private UUID accountId;
    private String accountNumber;
    private String message;
}