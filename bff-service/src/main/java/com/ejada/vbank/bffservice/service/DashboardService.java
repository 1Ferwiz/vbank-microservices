package com.ejada.vbank.bffservice.service;

import com.ejada.vbank.bffservice.dto.*;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@Service
public class DashboardService {

    private final WebClient userServiceClient;
    private final WebClient accountServiceClient;
    private final WebClient transactionServiceClient;

    public DashboardService(WebClient userServiceClient,
                            WebClient accountServiceClient,
                            WebClient transactionServiceClient) {
        this.userServiceClient = userServiceClient;
        this.accountServiceClient = accountServiceClient;
        this.transactionServiceClient = transactionServiceClient;
    }

    public Mono<DashboardResponse> getDashboard(UUID userId) {
        Mono<UserProfileResponse> userProfileMono = userServiceClient.get()
                .uri("/users/{userId}/profile", userId)
                .retrieve()
                .bodyToMono(UserProfileResponse.class);

        Mono<List<AccountDashboard>> accountsMono = accountServiceClient.get()
                .uri("/users/{userId}/accounts", userId)
                .retrieve()
                .bodyToFlux(AccountResponse.class)
                .onErrorResume(WebClientResponseException.NotFound.class, e -> Mono.empty()) // Handle user having no accounts
                .flatMap(account -> {
                    Mono<List<TransactionResponse>> transactionsMono = transactionServiceClient.get()
                            .uri("/accounts/{accountId}/transactions", account.getAccountId())
                            .retrieve()
                            .bodyToFlux(TransactionResponse.class)
                            .collectList()
                            .onErrorResume(WebClientResponseException.NotFound.class, e -> Mono.just(List.of())); // Ignore if no transactions

                    return transactionsMono.map(transactions -> new AccountDashboard(
                            account.getAccountId(),
                            account.getAccountNumber(),
                            account.getAccountType(),
                            account.getBalance(),
                            transactions
                    ));
                })
                .collectList();

        return Mono.zip(userProfileMono, accountsMono)
                .map(tuple -> {
                    UserProfileResponse profile = tuple.getT1();
                    List<AccountDashboard> accounts = tuple.getT2();
                    return new DashboardResponse(
                            profile.getUserId(),
                            profile.getUsername(),
                            profile.getEmail(),
                            profile.getFirstName(),
                            profile.getLastName(),
                            accounts
                    );
                });
    }
}
