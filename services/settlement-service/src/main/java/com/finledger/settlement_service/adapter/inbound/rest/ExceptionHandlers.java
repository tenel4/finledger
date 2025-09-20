package com.finledger.settlement_service.adapter.inbound.rest;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.finledger.settlement_service.adapter.inbound.rest.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ExceptionHandlers {
  private static final Logger log = LoggerFactory.getLogger(ExceptionHandlers.class);

  private String traceId() {
    return MDC.get("traceId");
  }

  private String correlationId() {
    return MDC.get("correlationId");
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ResponseEntity<ErrorResponse> handleValidationExceptions(
      MethodArgumentNotValidException ex, HttpServletRequest request) {
    log.info("Incoming trade 1: {}", request);

    List<String> errors =
        ex.getBindingResult().getFieldErrors().stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .toList();
    ErrorResponse errorResponse =
        ErrorResponse.of(
            HttpStatus.BAD_REQUEST.value(),
            "Validation Failed",
            errors,
            request.getRequestURI(),
            traceId(),
            correlationId());
    return ResponseEntity.badRequest().body(errorResponse);
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(
      HttpMessageNotReadableException ex, HttpServletRequest request) {
    Throwable cause = ex.getCause();
    log.info("Incoming trade 2: {}", request);
    if (cause instanceof InvalidFormatException invalidFormatException
        && invalidFormatException.getTargetType().isEnum()) {
      String field = invalidFormatException.getPath().getFirst().getFieldName();
      String msg =
          "Invalid value for field '"
              + field
              + "'. Valid values: "
              + String.join(
                  ", ",
                  List.of(invalidFormatException.getTargetType().getEnumConstants()).stream()
                      .map(Object::toString)
                      .toList());
      ErrorResponse errorResponse =
          ErrorResponse.of(
              HttpStatus.BAD_REQUEST.value(),
              "Invalid Enum Value",
              Collections.singletonList(msg),
              request.getRequestURI(),
              traceId(),
              correlationId());
      return ResponseEntity.badRequest().body(errorResponse);
    }
    ErrorResponse errorResponse =
        ErrorResponse.of(
            HttpStatus.BAD_REQUEST.value(),
            "Malformed JSON Request",
            Collections.singletonList(ex.getMessage()),
            request.getRequestURI(),
            traceId(),
            correlationId());
    return ResponseEntity.badRequest().body(errorResponse);
  }

  @ExceptionHandler(Exception.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public ResponseEntity<ErrorResponse> handleGenericException(
      Exception ex, HttpServletRequest request) {
    log.info("Incoming trade 3: {}", request);
    ErrorResponse errorResponse =
        ErrorResponse.of(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "Internal Server Error",
            Collections.singletonList("An unexpected error occurred"),
            request.getRequestURI(),
            traceId(),
            correlationId());
    return ResponseEntity.internalServerError().body(errorResponse);
  }
}
