package com.finledger.settlement_service.adapter.outbound.persistance.entity;

import com.finledger.settlement_service.domain.model.OutboxStatus;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "outbox_event")
@NoArgsConstructor
@Getter
@Setter
public class OutboxEventEntity {
  @Id private UUID id;

  @Column(nullable = false)
  private String type;

  @Column(name = "aggregate_type")
  private String aggregateType;

  @Column(name = "aggregate_id")
  private String aggregateId;

  @Column(name = "payload")
  private String payload;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private OutboxStatus status; // PENDING, RETRY, PROCESSING, SENT, DEAD

  @Column(name = "retry_count", nullable = false)
  private int retryCount;

  @Column(name = "next_attempt_at")
  private Instant nextAttemptAt;

  @Column(name = "last_error", length = 2000)
  private String lastError;

  @Column(name = "created_at")
  private Instant createdAt;

  @Column(name = "updated_at")
  private Instant updatedAt;

  public static OutboxEventEntity pending(
      UUID id, String type, String aggregateType, String aggregateId, String payload) {
    OutboxEventEntity e = new OutboxEventEntity();
    e.id = id;
    e.type = type;
    e.aggregateType = aggregateType;
    e.aggregateId = aggregateId;
    e.payload = payload;
    e.status = OutboxStatus.PENDING;
    e.retryCount = 0;
    e.nextAttemptAt = null;
    e.lastError = null;
    e.createdAt = Instant.now();
    e.updatedAt = null;
    return e;
  }
}
