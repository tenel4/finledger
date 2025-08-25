package com.finledger.settlement_service.model.dto;

import com.finledger.settlement_service.model.enums.Side;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateTradeRequest(
    @NotBlank String symbol,
    @NotNull Side side,
    @Positive Long quantity,
    @NotNull @Positive BigDecimal price,
    @NotBlank String currency,
    @NotNull UUID buyerAccountId,
    @NotNull UUID sellerAccountId
) {}