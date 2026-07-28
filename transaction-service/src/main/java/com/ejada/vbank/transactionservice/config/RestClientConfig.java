package com.ejada.vbank.transactionservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Bean
    public RestClient accountServiceRestClient(
            @Value("${account-service.base-url}") String accountServiceBaseUrl) {
        return RestClient.builder()
                .baseUrl(accountServiceBaseUrl)
                .build();
    }
}
