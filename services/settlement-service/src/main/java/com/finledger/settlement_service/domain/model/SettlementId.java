package com.finledger.settlement_service.domain.model;

import java.util.UUID;

public record SettlementId(UUID value) {
    public SettlementId {
        if (value == null) throw new IllegalArgumentException("SettlementId cannot be null");
    }
    public static SettlementId newId() { return new SettlementId(UUID.randomUUID()); }
}
