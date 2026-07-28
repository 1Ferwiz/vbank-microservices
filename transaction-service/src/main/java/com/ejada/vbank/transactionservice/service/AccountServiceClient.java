package com.ejada.vbank.transactionservice.service;

import com.ejada.vbank.transactionservice.dto.AccountTransferRequest;
import com.ejada.vbank.transactionservice.exception.TransferFailedException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Thin client around Account Service's PUT /accounts/transfer.
 * Expected contract (Account Service side, not yet built as of this writing):
 *   PUT /accounts/transfer
 *   Body:  { "fromAccountId": UUID, "toAccountId": UUID, "amount": BigDecimal }
 *   200 OK        -> debit + credit applied atomically
 *   404 NOT_FOUND -> one of the accounts doesn't exist
 *   422 / 409      -> insufficient funds or inactive account
 *   Any non-2xx or network failure here is treated as a failed transfer.
 */
@Component
public class AccountServiceClient {

    private final RestClient accountServiceRestClient;

    public AccountServiceClient(RestClient accountServiceRestClient) {
        this.accountServiceRestClient = accountServiceRestClient;
    }

    public void executeTransfer(UUID fromAccountId, UUID toAccountId, BigDecimal amount) {
        AccountTransferRequest request = new AccountTransferRequest(fromAccountId, toAccountId, amount);

        try {
            accountServiceRestClient.put()
                    .uri("/accounts/transfer")
                    .body(request)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientResponseException ex) {
            // Account Service responded, but rejected the transfer (e.g. insufficient funds,
            // inactive account, account not found).
            com.ejada.vbank.transactionservice.dto.ErrorResponse errorResponse = null;
            try {
                errorResponse = ex.getResponseBodyAs(com.ejada.vbank.transactionservice.dto.ErrorResponse.class);
            } catch (Exception e) {
                // Ignore parsing errors
            }
            String message = (errorResponse != null && errorResponse.getMessage() != null)
                    ? errorResponse.getMessage()
                    : ex.getStatusText();

            throw new TransferFailedException(
                    "Account Service rejected the transfer: " + message);
        } catch (RestClientException ex) {
            // Account Service unreachable (network error, timeout, service down).
            throw new TransferFailedException(
                    "Could not reach Account Service to complete the transfer.");
        }
    }
}
