package com.finledger.settlement_service.adapter.inbound.rest.dto;

import com.finledger.settlement_service.domain.model.Settlement;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record GetSettlementsResponse(
    UUID id,
    UUID tradeId,
    LocalDate valueDate,
    BigDecimal grossAmount,
    BigDecimal fees,
    BigDecimal netAmount,
    String currency,
    Settlement.Status status,
    UUID messageId) {}
