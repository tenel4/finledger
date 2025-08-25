package com.finledger.settlement_service.model.event;

import com.finledger.settlement_service.model.enums.Side;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TradeCreatedEvent (
    UUID messageKey,
    UUID tradeId,
    String symbol,
    Side side,
    Long quantity,
    BigDecimal price,
    String currency,
    UUID buyerAccountId,
    UUID sellerAccountId,
    Instant tradeTime
){}
