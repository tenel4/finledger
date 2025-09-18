package com.finledger.settlement_service.adapter.inbound.rest.dto;

import com.finledger.settlement_service.domain.model.Trade;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record GetTradesResponse(
        UUID id,
        String symbol,
        Trade.Side side,
        long quantity,
        BigDecimal price,
        String currency,
        UUID buyerAccountId,
        UUID sellerAccountId,
        Instant tradeTime
) {
}
