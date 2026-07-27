package com.ejada.vbank.accountservice.dto;

import com.ejada.vbank.accountservice.entity.AccountType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
public class CreateAccountRequest {

    @NotNull
    private UUID userId;

    @NotNull
    private AccountType accountType;

    @NotNull
    @DecimalMin(value = "0.00", message = "initialBalance must not be negative")
    private BigDecimal initialBalance;
}