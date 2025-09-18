package com.finledger.ledger_service.adapter.inbound.rest;

import java.math.BigDecimal;
import java.util.UUID;

public record LedgerSummaryResponse(
        UUID accountId,
        String currency,
        BigDecimal sum
) {}
