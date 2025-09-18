package com.finledger.settlement_service.adapter.outbound.persistance.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "processed_message")
@Getter
@Setter
@NoArgsConstructor
public class ProcessedMessageEntity {
    @Id
    @Column(name = "message_key")
    private UUID messageId;
    @Column(name = "processed_at", nullable = false)
    private Instant processedAt = Instant.now();
}
