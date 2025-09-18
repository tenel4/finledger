package com.finledger.ledger_service.adapter.outbound.persistence;

import com.finledger.ledger_service.application.port.outbound.ProcessedMessageRepositoryPort;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ProcessedMessageRepositoryAdapter implements ProcessedMessageRepositoryPort {
    private final ProcessedMessageJpaRepository repo;

    public ProcessedMessageRepositoryAdapter(ProcessedMessageJpaRepository repo) {
        this.repo = repo;
    }

    @Override
    @Transactional
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
