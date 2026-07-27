package com.ejada.vbank.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class RegisterResponse {
    private UUID userId;
    private String username;
    private String message;
}