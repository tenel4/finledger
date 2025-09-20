package com.finledger.ledger_service.adapter.outbound.persistence;

import com.finledger.ledger_service.application.port.outbound.ProcessedMessageRepositoryPort;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class ProcessedMessageRepositoryAdapter implements ProcessedMessageRepositoryPort {
  private final ProcessedMessageJpaRepository repo;

  public ProcessedMessageRepositoryAdapter(ProcessedMessageJpaRepository repo) {
    this.repo = repo;
  }

  @Override
  public boolean markProcessedIfNew(UUID messageId) {
    if (repo.existsById(messageId)) {
      return false; // already processed
    }
    ProcessedMessageEntity entity = new ProcessedMessageEntity();
    entity.setMessageId(messageId);
    repo.save(entity);
    return true;
  }
}
