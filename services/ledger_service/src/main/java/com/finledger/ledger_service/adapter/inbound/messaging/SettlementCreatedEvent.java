package com.finledger.ledger_service.adapter.inbound.messaging;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record SettlementCreatedEvent(
        UUID messageId,
        UUID settlementId,
        LocalDate valueDate,
        BigDecimal grossAmount,
        BigDecimal fees,
        BigDecimal netAmount,
        String currency,
        UUID buyerAccountId,
        UUID sellerAccountId
) {}
