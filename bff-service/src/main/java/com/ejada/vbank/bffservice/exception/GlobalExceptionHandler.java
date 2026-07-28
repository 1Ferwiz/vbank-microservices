package com.ejada.vbank.bffservice.exception;

import com.ejada.vbank.bffservice.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(WebClientResponseException.NotFound.class)
    public ResponseEntity<ErrorResponse> handleNotFound(WebClientResponseException.NotFound ex) {
        return build(HttpStatus.NOT_FOUND, "User profile not found.");
    }

    @ExceptionHandler(WebClientResponseException.class)
    public ResponseEntity<ErrorResponse> handleDownstreamError(WebClientResponseException ex) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to retrieve dashboard data due to an issue with downstream services.");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericError(Exception ex) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred.");
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String message) {
        return ResponseEntity.status(status)
                .body(new ErrorResponse(status.value(), status.getReasonPhrase(), message));
    }
}
