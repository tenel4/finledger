package com.finledger.settlement_service.adapter.inbound.rest.dto;

import com.finledger.settlement_service.domain.model.Trade;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.UUID;

public record CreateTradeRequest(
    @NotBlank String symbol,
    @NotNull Trade.Side side,
    @Positive long quantity,
    @NotNull @DecimalMin("0.0") BigDecimal price,
    @NotBlank String currency,
    @NotNull UUID buyerAccountId,
    @NotNull UUID sellerAccountId) {}
