package com.finledger.settlement_service.domain.event;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

public final class SettlementCreatedEvent extends BaseDomainEvent implements AggregateDomainEvent {

    private final UUID settlementId;
    private final UUID buyerAccountId;
    private final UUID sellerAccountId;
    private final BigDecimal netAmount;
    private final String currency;

    public SettlementCreatedEvent(UUID settlementId,
                                  UUID buyerAccountId,
                                  UUID sellerAccountId,
                                  BigDecimal netAmount,
                                  String currency) {
        super(); // auto-generates eventId and occurredAt
        this.settlementId = Objects.requireNonNull(settlementId, "Settlement ID cannot be null");
        this.buyerAccountId = Objects.requireNonNull(buyerAccountId, "Buyer Account ID cannot be null");
        this.sellerAccountId = Objects.requireNonNull(sellerAccountId, "Seller Account ID cannot be null");
        this.netAmount = Objects.requireNonNull(netAmount, "Net Amount cannot be null");
        this.currency = Objects.requireNonNull(currency, "Currency cannot be null");
    }

    public UUID settlementId() { return settlementId; }
    public UUID buyerAccountId() { return buyerAccountId; }
    public UUID sellerAccountId() { return sellerAccountId; }
    public BigDecimal netAmount() { return netAmount; }
    public String currency() { return currency; }
    @Override
    public String eventType() { return "settlement.created"; }
    @Override
    public String aggregateType() { return "Settlement"; }
    @Override
    public UUID aggregateId() { return settlementId; }
}
