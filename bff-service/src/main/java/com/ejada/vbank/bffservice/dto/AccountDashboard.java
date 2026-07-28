package com.ejada.vbank.bffservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AccountDashboard {
    private UUID accountId;
    private String accountNumber;
    private String accountType;
    private BigDecimal balance;
    private List<TransactionResponse> transactions;
}
