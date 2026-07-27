package com.ejada.vbank.userservice.service;

import com.ejada.vbank.userservice.dto.*;
import com.ejada.vbank.userservice.entity.User;
import com.ejada.vbank.userservice.exception.DuplicateResourceException;
import com.ejada.vbank.userservice.exception.InvalidCredentialsException;
import com.ejada.vbank.userservice.exception.ResourceNotFoundException;
import com.ejada.vbank.userservice.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())
                || userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Username or email already exists.");
        }

        String hashedPassword = passwordEncoder.encode(request.getPassword());

        User user = new User(
                request.getUsername(),
                request.getEmail(),
                hashedPassword,
                request.getFirstName(),
                request.getLastName()
        );

        User saved = userRepository.saveAndFlush(user);
        return new RegisterResponse(saved.getId(), saved.getUsername(), "User registered successfully.");
    }

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid username or password."));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new InvalidCredentialsException("Invalid username or password.");
        }

        return new LoginResponse(user.getId(), user.getUsername());
    }

    public UserProfileResponse getProfile(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User with ID " + userId + " not found."));
        return new UserProfileResponse(
                user.getId(), user.getUsername(), user.getEmail(),
                user.getFirstName(), user.getLastName()
        );
    }
}