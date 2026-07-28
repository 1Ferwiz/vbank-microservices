package com.ejada.vbank.transactionservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class ExecuteTransferRequest {

    @NotNull
    private UUID transactionId;
}