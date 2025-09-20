package com.finledger.ledger_service.adapter.outbound.persistence;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "processed_message")
@Getter
@Setter
@NoArgsConstructor
public class ProcessedMessageEntity {
  @Id
  @Column(name = "message_key", nullable = false)
  private UUID messageId;

  @Column(name = "processed_at", nullable = false)
  private OffsetDateTime processedAt;

  public ProcessedMessageEntity(UUID messageKey) {
    this.messageId = messageKey;
    this.processedAt = OffsetDateTime.now();
  }

  @PrePersist
  protected void onCreate() {
    if (this.processedAt == null) {
      this.processedAt = OffsetDateTime.now();
    }
  }
}
