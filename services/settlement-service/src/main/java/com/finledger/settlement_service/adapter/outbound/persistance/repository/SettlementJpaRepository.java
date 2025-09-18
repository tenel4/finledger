package com.finledger.settlement_service.adapter.outbound.persistance.repository;

import com.finledger.settlement_service.adapter.outbound.persistance.entity.SettlementEntity;
import com.finledger.settlement_service.domain.model.Settlement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface SettlementJpaRepository extends JpaRepository<SettlementEntity, UUID> {
    List<SettlementEntity> findByStatusAndValueDate(Settlement.Status status, LocalDate valueDate);
    List<SettlementEntity> findByStatus(String status);
    List<SettlementEntity> findByValueDate(LocalDate valueDate);
}
