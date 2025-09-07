package com.finledger.settlement_service.infrastructure.persistance.repository;

import com.finledger.settlement_service.domain.port.ProcessedMessageRepository;
import com.finledger.settlement_service.infrastructure.persistance.entity.ProcessedMessageEntity;
import org.springframework.stereotype.Component;

@Component
public class ProcessedMessageRepositoryAdapter implements ProcessedMessageRepository {
    private final ProcessedMessageEntityJpaRepository jpaRepository;

    public ProcessedMessageRepositoryAdapter(ProcessedMessageEntityJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public void save(ProcessedMessageEntity processedMessageEntity) {
        jpaRepository.save(processedMessageEntity);
    }

    @Override
    public boolean existsById(java.util.UUID messageId) {
        return jpaRepository.existsById(messageId);
    }
}
