package com.finledger.settlement_service.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "processed_message")
@Data
public class ProcessedMessage {
    @Id
    @Column(name = "message_key")
    private UUID messageKey;

    @Column(name = "processed_at", nullable = false)
    private Instant processedAt = Instant.now();
}
