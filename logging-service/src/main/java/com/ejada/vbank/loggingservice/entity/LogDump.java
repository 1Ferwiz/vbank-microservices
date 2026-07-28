package com.ejada.vbank.loggingservice.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "log_dump")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LogDump {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(name = "message_type", nullable = false, length = 20)
    private String messageType;

    @Column(name = "event_time", nullable = false)
    private LocalDateTime eventTime;

    @CreationTimestamp
    @Column(name = "received_at", nullable = false, updatable = false)
    private LocalDateTime receivedAt;

    public LogDump(String message, String messageType, LocalDateTime eventTime) {
        this.message = message;
        this.messageType = messageType;
        this.eventTime = eventTime;
    }
}