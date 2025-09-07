package com.finledger.settlement_service.application.dto;

import java.util.UUID;

public record CreateTradeResponse(
        UUID tradeId,
        String messageKey
) {}
