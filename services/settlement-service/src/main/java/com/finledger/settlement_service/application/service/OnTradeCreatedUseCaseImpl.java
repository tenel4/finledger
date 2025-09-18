package com.finledger.settlement_service.application.service;

import com.finledger.settlement_service.application.dto.SettlementCreatedEventDto;
import com.finledger.settlement_service.application.dto.TradeCreatedEventDto;
import com.finledger.settlement_service.application.port.outbound.EventPublisher;
import com.finledger.settlement_service.application.port.outbound.TradeRepositoryPort;
import com.finledger.settlement_service.application.port.inbound.OnTradeCreatedUseCase;
import com.finledger.settlement_service.domain.event.SettlementCreatedEvent;
import com.finledger.settlement_service.domain.exception.SettlementCreationException;
import com.finledger.settlement_service.domain.model.*;
import com.finledger.settlement_service.application.port.outbound.SettlementRepositoryPort;
import com.finledger.settlement_service.domain.service.ValueDateCalculator;
import com.finledger.settlement_service.domain.value.Money;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

@Service
public class OnTradeCreatedUseCaseImpl implements OnTradeCreatedUseCase {
    private final TradeRepositoryPort tradeRepositoryPort;
    private final SettlementRepositoryPort settlementRepositoryPort;
    private final ValueDateCalculator valueDateCalculator;
    private final EventPublisher eventPublisher;

    public OnTradeCreatedUseCaseImpl(TradeRepositoryPort tradeRepositoryPort,
                                     SettlementRepositoryPort settlementRepositoryPort,
                                     ValueDateCalculator valueDateCalculator,
                                     EventPublisher eventPublisher) {
        this.tradeRepositoryPort = tradeRepositoryPort;
        this.settlementRepositoryPort = settlementRepositoryPort;
        this.valueDateCalculator = valueDateCalculator;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public void execute(TradeCreatedEventDto event) {
        Optional<Trade> tradeOpt = tradeRepositoryPort.findById(event.tradeId());
        if (tradeOpt.isEmpty()) {
            throw new IllegalStateException("Trade not found for id: " + event.tradeId());
        }
        Trade trade = tradeOpt.get();

        LocalDate valueDate = valueDateCalculator.calculate(event.tradeTime());
        Money grossAmount = Money.of(new BigDecimal(event.grossAmount()), event.currency());
        Settlement settlement = Settlement.createNew(event.tradeId(), valueDate, grossAmount,
                Settlement.Status.PENDING, event.eventId());
        settlementRepositoryPort.save(settlement);

        SettlementCreatedEvent domainEvent = new SettlementCreatedEvent(
                settlement.id(),
                trade.buyerAccountId(),
                trade.sellerAccountId(),
                settlement.netAmount().amount(),
                settlement.netAmount().currency().toString());
        SettlementCreatedEventDto outboundDto = SettlementCreatedEventDto.fromDomain(domainEvent);

        try {
            eventPublisher.publish(outboundDto);
        } catch (Exception e) {
            throw new SettlementCreationException("Failed to create settlement due to event persistence error", e);
        }

    }
}
