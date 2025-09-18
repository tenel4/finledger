package com.finledger.ledger_service.adapter.outbound.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProcessedMessageJpaRepository extends JpaRepository<ProcessedMessageEntity, UUID> {
}
