package com.finledger.settlement_service.infrastructure.persistance.repository;

import com.finledger.settlement_service.domain.model.Settlement;
import com.finledger.settlement_service.domain.port.SettlementRepository;
import com.finledger.settlement_service.infrastructure.persistance.entity.SettlementEntity;
import org.springframework.stereotype.Component;

@Component
public class SettlementRepositoryAdapter implements SettlementRepository {
    private final SettlementEntityJpaRepository jpaRepository;

    public SettlementRepositoryAdapter(SettlementEntityJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Settlement save(Settlement settlement) {
        SettlementEntity entity = new SettlementEntity();
        entity.setId(settlement.id().value());
        entity.setTradeId(settlement.tradeId().value());
        entity.setValueDate(settlement.valueDate());
        entity.setGrossAmount(settlement.grossAmount().amount());
        entity.setFees(settlement.fees().amount());
        entity.setNetAmount(settlement.netAmount().amount());
        entity.setStatus(settlement.status());
        entity.setMessageId(settlement.messageKey());

        jpaRepository.save(entity);

        return settlement;
    }
}
