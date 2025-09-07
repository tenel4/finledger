package com.finledger.settlement_service.web.dto;

import java.time.OffsetDateTime;
import java.util.List;

public record ErrorResponse(
        OffsetDateTime timestamp,
        int status,
        String error,
        List<String> messages,
        String path,
        String traceId,
        String correlationId
) {
    public static ErrorResponse of(int status, String error, List<String> messages, String path, String traceId, String correlationId) {
        return new ErrorResponse(OffsetDateTime.now(), status, error, messages, path, traceId, correlationId);
    }
}
