package com.finledger.settlement_service.adapter.outbound.persistance.repository;

import com.finledger.settlement_service.adapter.outbound.persistance.entity.ProcessedMessageEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessedMessageJpaRepository
    extends JpaRepository<ProcessedMessageEntity, UUID> {}
