package com.finledger.settlement_service.infrastructure.persistance.repository;

import com.finledger.settlement_service.infrastructure.persistance.entity.TradeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TradeEntityJpaRepository extends JpaRepository<TradeEntity, UUID> {
}
