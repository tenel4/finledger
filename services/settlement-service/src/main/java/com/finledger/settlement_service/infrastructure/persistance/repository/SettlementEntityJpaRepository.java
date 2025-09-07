package com.finledger.settlement_service.infrastructure.persistance.repository;

import com.finledger.settlement_service.infrastructure.persistance.entity.SettlementEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SettlementEntityJpaRepository extends JpaRepository<SettlementEntity, UUID> {
}
