package com.finledger.settlement_service.common.exception;

public class JsonDeserializationException extends RuntimeException {
  public JsonDeserializationException(String message, Throwable cause) {
    super(message, cause);
  }
}
