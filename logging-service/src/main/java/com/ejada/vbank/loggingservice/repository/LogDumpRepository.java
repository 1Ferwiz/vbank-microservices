package com.ejada.vbank.loggingservice.repository;

import com.ejada.vbank.loggingservice.entity.LogDump;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LogDumpRepository extends JpaRepository<LogDump, Long> {
}