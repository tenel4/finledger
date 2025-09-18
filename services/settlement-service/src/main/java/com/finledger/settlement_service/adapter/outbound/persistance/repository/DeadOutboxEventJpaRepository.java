package com.finledger.settlement_service.adapter.outbound.persistance.repository;

import com.finledger.settlement_service.adapter.outbound.persistance.entity.DeadOutboxEventEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DeadOutboxEventJpaRepository extends JpaRepository<DeadOutboxEventEntity, UUID> {
    Page<DeadOutboxEventEntity> findAllByOrderByDeadAtDesc(Pageable pageable);
}
