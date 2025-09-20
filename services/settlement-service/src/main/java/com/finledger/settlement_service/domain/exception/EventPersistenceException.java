package com.finledger.settlement_service.domain.exception;

public class EventPersistenceException extends RuntimeException {
  public EventPersistenceException(String message, Throwable cause) {
    super(message, cause);
  }
}
