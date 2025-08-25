package com.finledger.settlement_service.exception;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.finledger.settlement_service.model.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .toList();
        ErrorResponse errorResponse = new ErrorResponse(OffsetDateTime.now(), HttpStatus.BAD_REQUEST.value(), errors);
        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        Throwable cause = ex.getCause();
        if (cause instanceof InvalidFormatException invalidFormatException && invalidFormatException.getTargetType().isEnum()) {
            String field = invalidFormatException.getPath().getFirst().getFieldName();
            String msg = "Invalid value for field '" + field + "'. Valid values: " +
                    String.join(", ", List.of(invalidFormatException.getTargetType().getEnumConstants()).stream().map(Object::toString).toList());
            ErrorResponse errorResponse = new ErrorResponse(OffsetDateTime.now(), HttpStatus.BAD_REQUEST.value(), Collections.singletonList(msg));
            return ResponseEntity.badRequest().body(errorResponse);

        }
        ErrorResponse errorResponse = new ErrorResponse(OffsetDateTime.now(), HttpStatus.BAD_REQUEST.value(), Collections.singletonList(ex.getMessage()));
        return ResponseEntity.badRequest().body(errorResponse);
    }
}