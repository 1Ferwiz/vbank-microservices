package com.ejada.vbank.accountservice.job;

import com.ejada.vbank.accountservice.service.AccountService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class StaleAccountJob {

    private final AccountService accountService;

    public StaleAccountJob(AccountService accountService) {
        this.accountService = accountService;
    }

    @Scheduled(fixedRate = 3_600_000) // every 1 hour
    public void run() {
        accountService.inactivateStaleAccounts();
    }
}