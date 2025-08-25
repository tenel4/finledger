package com.finledger.settlement_service.repository;

import com.finledger.settlement_service.model.Settlement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SettlementRepository extends JpaRepository<Settlement, UUID> {
}
