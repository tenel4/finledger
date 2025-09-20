package com.finledger.settlement_service.common.exception;

public class EodReconciliationException extends RuntimeException {
  public EodReconciliationException(String message, Throwable cause) {
    super(message, cause);
  }
}
