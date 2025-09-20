package com.finledger.ledger_service.adapter.inbound.rest;

import com.finledger.ledger_service.domain.model.LedgerEntryReferenceType;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record LedgerEntryResponse(
    UUID id,
    Instant entryTime,
    UUID accountId,
    String currency,
    BigDecimal amountSigned,
    LedgerEntryReferenceType referenceType,
    UUID referenceId) {}
