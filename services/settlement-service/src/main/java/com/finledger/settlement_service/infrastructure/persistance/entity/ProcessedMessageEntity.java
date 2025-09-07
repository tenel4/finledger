package com.finledger.settlement_service.infrastructure.persistance.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "processed_message")
@NoArgsConstructor
@Getter
public class ProcessedMessageEntity {
    @Id
    @Column(name = "message_key", nullable = false)
    private UUID messageKey;
    @Column(name = "processed_at", nullable = false)
    private OffsetDateTime processedAt;

    public ProcessedMessageEntity(UUID messageKey) {
        this.messageKey = messageKey;
        this.processedAt = OffsetDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        if (this.processedAt == null) {
            this.processedAt = OffsetDateTime.now();
        }
    }

}
