package com.ejada.vbank.common.logging;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

public class RequestResponseLoggingFilter extends OncePerRequestFilter {

    private static final int MAX_CACHED_BODY_BYTES = 65536; // 64 KB — plenty for JSON API payloads in this project

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String topic;

    public RequestResponseLoggingFilter(KafkaTemplate<String, String> kafkaTemplate, String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return request.getRequestURI().startsWith("/actuator");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request, MAX_CACHED_BODY_BYTES);
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);

        try {
            filterChain.doFilter(wrappedRequest, wrappedResponse);
        } finally {
            String requestBody = new String(wrappedRequest.getContentAsByteArray(), StandardCharsets.UTF_8);
            String responseBody = new String(wrappedResponse.getContentAsByteArray(), StandardCharsets.UTF_8);

            publish(requestBody, "Request");
            publish(responseBody, "Response");

            wrappedResponse.copyBodyToResponse();
        }
    }

    private void publish(String body, String messageType) {
        try {
            LogMessage logMessage = new LogMessage(body, messageType, Instant.now().toString());
            String payload = objectMapper.writeValueAsString(logMessage);
            kafkaTemplate.send(topic, payload);
        } catch (Exception ex) {
            logger.warn("Failed to publish " + messageType + " log message to Kafka", ex);
        }
    }
}