package com.finledger.settlement_service.domain.model;

import java.util.UUID;

public record TradeId(UUID value) {
    public TradeId {
        if (value == null) throw new IllegalArgumentException("TradeId cannot be null");
    }
    public static TradeId newId() { return new TradeId(UUID.randomUUID()); }
}
