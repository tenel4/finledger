package com.finledger.settlement_service.infrastructure.persistance.repository;

import com.finledger.settlement_service.infrastructure.persistance.entity.ProcessedMessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProcessedMessageEntityJpaRepository extends JpaRepository<ProcessedMessageEntity, UUID> {
}
