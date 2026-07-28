package com.ejada.vbank.bffservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponse {
    private UUID userId;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private List<AccountDashboard> accounts;
}
