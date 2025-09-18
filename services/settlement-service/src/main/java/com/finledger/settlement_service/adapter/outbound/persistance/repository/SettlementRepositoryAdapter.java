package com.finledger.settlement_service.adapter.outbound.persistance.repository;

import com.finledger.settlement_service.adapter.outbound.persistance.entity.SettlementEntity;
import com.finledger.settlement_service.domain.value.Money;
import com.finledger.settlement_service.domain.model.Settlement;
import com.finledger.settlement_service.application.port.outbound.SettlementRepositoryPort;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class SettlementRepositoryAdapter implements SettlementRepositoryPort {
    private final SettlementJpaRepository repo;

    public SettlementRepositoryAdapter(SettlementJpaRepository repo) {
        this.repo = repo;
    }

    @Override
    @Transactional
    public Settlement save(Settlement settlement) {
        SettlementEntity entity = toEntity(settlement);
        repo.save(entity);
        return settlement;
    }

    @Override
    public List<Settlement> find(Settlement.Status status, LocalDate date) {
        if (status != null && date != null) {
            return repo.findByStatusAndValueDate(status, date)
                    .stream().map(this::toDomain).toList();
        } else if (status != null) {
            return repo.findByStatus(status.name())
                    .stream().map(this::toDomain).toList();
        } else if (date != null) {
            return repo.findByValueDate(date)
                    .stream().map(this::toDomain).toList();
        } else {
            return repo.findAll().stream().map(this::toDomain).toList();
        }
    }

    private SettlementEntity toEntity(Settlement s) {
        SettlementEntity e = new SettlementEntity();
        e.setId(s.id());
        e.setTradeId(s.tradeId());
        e.setValueDate(s.valueDate());
        e.setGrossAmount(s.grossAmount().amount());
        e.setFees(s.fees().amount());
        e.setNetAmount(s.netAmount().amount());
        e.setCurrency(s.grossAmount().currency().toString());
        e.setStatus(s.status());
        e.setMessageId(s.messageId());
        return e;
    }

    private Settlement toDomain(SettlementEntity e) {
        Money grossAmount = Money.of(e.getGrossAmount(), e.getCurrency());
        Money fees = Money.of(e.getFees(), e.getCurrency());
        Money netAmount = Money.of(e.getNetAmount(), e.getCurrency());
        return Settlement.rehydrate(
                e.getId(),
                e.getTradeId(),
                e.getValueDate(),
                grossAmount,
                fees,
                netAmount,
                e.getStatus(),
                e.getMessageId()
        );
    }
}
