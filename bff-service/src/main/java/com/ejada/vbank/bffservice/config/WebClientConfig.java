package com.ejada.vbank.bffservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient userServiceClient(@Value("${microservices.user-service.url}") String baseUrl) {
        return WebClient.builder().baseUrl(baseUrl).build();
    }

    @Bean
    public WebClient accountServiceClient(@Value("${microservices.account-service.url}") String baseUrl) {
        return WebClient.builder().baseUrl(baseUrl).build();
    }

    @Bean
    public WebClient transactionServiceClient(@Value("${microservices.transaction-service.url}") String baseUrl) {
        return WebClient.builder().baseUrl(baseUrl).build();
    }
}
