package com.ejada.vbank.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class UserProfileResponse {
    private UUID userId;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
}