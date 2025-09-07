package com.finledger.settlement_service.domain.port;

import com.finledger.settlement_service.infrastructure.persistance.entity.ProcessedMessageEntity;

import java.util.UUID;

public interface ProcessedMessageRepository {
    void save(ProcessedMessageEntity processedMessageEntity);
    boolean existsById(UUID messageId);
}
