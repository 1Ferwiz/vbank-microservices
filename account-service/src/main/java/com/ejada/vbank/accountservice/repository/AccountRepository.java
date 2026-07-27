package com.ejada.vbank.accountservice.repository;

import com.ejada.vbank.accountservice.entity.Account;
import com.ejada.vbank.accountservice.entity.AccountStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface AccountRepository extends JpaRepository<Account, UUID> {

    boolean existsByAccountNumber(String accountNumber);

    List<Account> findByUserId(UUID userId);

    List<Account> findByStatusAndLastActivityAtBefore(AccountStatus status, LocalDateTime cutoff);
}