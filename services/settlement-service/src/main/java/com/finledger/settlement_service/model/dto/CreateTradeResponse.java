package com.finledger.settlement_service.model.dto;

import java.time.Instant;
import java.util.UUID;

public record CreateTradeResponse (
    UUID id,
    Instant tradeTime,
    UUID messageKey
) {}
