package com.ejada.vbank.userservice.service;

import com.ejada.vbank.userservice.dto.LoginRequest;
import com.ejada.vbank.userservice.dto.RegisterRequest;
import com.ejada.vbank.userservice.dto.UserResponse;
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
    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("Username already taken: " + request.getUsername());
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already registered: " + request.getEmail());
        }

        String hashedPassword = passwordEncoder.encode(request.getPassword());

        User user = new User(
                request.getUsername(),
                request.getEmail(),
                hashedPassword,
                request.getFirstName(),
                request.getLastName()
        );

        User saved = userRepository.save(user);
        return toResponse(saved);
    }

    public UserResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid username or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new InvalidCredentialsException("Invalid username or password");
        }

        return toResponse(user);
    }

    public UserResponse getById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
        return toResponse(user);
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getCreatedAt()
        );
    }
}