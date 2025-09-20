package com.finledger.ledger_service.adapter.outbound.persistence;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessedMessageJpaRepository
    extends JpaRepository<ProcessedMessageEntity, UUID> {}
