package com.finledger.settlement_service.adapter.outbound.persistance.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "dead_outbox_event")
@Getter
@Setter
@NoArgsConstructor
public class DeadOutboxEventEntity {
    @Id
    private UUID id;
    @Column(nullable = false)
    private String type;
    @Column(nullable = false, length = 10000)
    private String payload;
    @Column(name = "retry_count", nullable = false)
    private int retryCount;
    @Column(name = "last_error", length = 2000)
    private String lastError;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
    @Column(name = "dead_at", nullable = false)
    private Instant deadAt;
}
