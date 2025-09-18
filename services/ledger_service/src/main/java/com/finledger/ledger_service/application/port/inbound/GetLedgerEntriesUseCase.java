package com.finledger.ledger_service.application.port.inbound;

import com.finledger.ledger_service.domain.model.LedgerEntry;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface GetLedgerEntriesUseCase {
    List<LedgerEntry> execute(UUID accountId, Instant from, Instant to);
}
