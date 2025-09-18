package com.finledger.settlement_service.domain.exception;

public class SettlementCreationException extends RuntimeException {
    public SettlementCreationException(String message, Throwable cause) {
        super(message, cause);
    }
}
