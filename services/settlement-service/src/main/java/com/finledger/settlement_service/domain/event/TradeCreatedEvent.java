package com.finledger.settlement_service.domain.event;

import com.finledger.settlement_service.domain.model.Trade;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public final class TradeCreatedEvent extends BaseDomainEvent implements AggregateDomainEvent {
    private final UUID tradeId;
    private final BigDecimal grossAmount;
    private final String currency;
    private final Instant tradeTime;

    public TradeCreatedEvent(Trade trade) {
        super();
        this.tradeId = trade.id();
        this.grossAmount = trade.grossAmount().amount();
        this.currency = trade.price().currency().getCurrencyCode();
        this.tradeTime = trade.tradeTime();
    }

    public UUID tradeId() { return tradeId; }
    public BigDecimal grossAmount() { return grossAmount; }
    public String currency() { return currency; }
    public Instant tradeTime() { return tradeTime; }
    @Override
    public String eventType() { return "trade.created"; }
    @Override
    public String aggregateType() { return "Trade"; }
    @Override
    public UUID aggregateId() { return tradeId; }
}
