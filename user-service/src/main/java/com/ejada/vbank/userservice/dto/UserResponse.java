package com.ejada.vbank.userservice.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public class UserResponse {

    private UUID id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private LocalDateTime createdAt;

    public UserResponse(UUID id, String username, String email, String firstName, String lastName, LocalDateTime createdAt) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}