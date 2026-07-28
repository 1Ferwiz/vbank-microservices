package com.ejada.vbank.loggingservice.consumer;

import com.ejada.vbank.loggingservice.entity.LogDump;
import com.ejada.vbank.loggingservice.repository.LogDumpRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Component
public class LogMessageConsumer {

    private static final Logger log = LoggerFactory.getLogger(LogMessageConsumer.class);

    private final LogDumpRepository logDumpRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public LogMessageConsumer(LogDumpRepository logDumpRepository) {
        this.logDumpRepository = logDumpRepository;
    }

    private record IncomingLogMessage(String message, String messageType, String dateTime) {}

    @KafkaListener(topics = "${vbank.logging.kafka.topic:vbank-logs}", groupId = "logging-service")
    public void consume(String payload) {
        try {
            IncomingLogMessage incoming = objectMapper.readValue(payload, IncomingLogMessage.class);
            LocalDateTime eventTime = LocalDateTime.ofInstant(
                    Instant.parse(incoming.dateTime()), ZoneOffset.UTC);

            LogDump logDump = new LogDump(incoming.message(), incoming.messageType(), eventTime);
            logDumpRepository.save(logDump);
        } catch (Exception ex) {
            log.warn("Failed to process log message, skipping: {}", payload, ex);
        }
    }
}