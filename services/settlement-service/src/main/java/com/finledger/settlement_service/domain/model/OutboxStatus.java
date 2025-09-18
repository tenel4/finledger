package com.finledger.settlement_service.domain.model;

public enum OutboxStatus {
    PENDING,
    RETRY,
    PROCESSING,
    SENT,
    DEAD;

    public boolean isTerminal() {
        return this == SENT || this == DEAD;
    }
}
