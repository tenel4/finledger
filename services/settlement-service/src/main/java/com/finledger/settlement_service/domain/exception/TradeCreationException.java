package com.finledger.settlement_service.domain.exception;

public class TradeCreationException extends RuntimeException {
  public TradeCreationException(String message, Throwable cause) {
    super(message, cause);
  }
}
