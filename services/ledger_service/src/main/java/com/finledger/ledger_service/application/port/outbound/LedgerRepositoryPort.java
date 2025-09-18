package com.finledger.ledger_service.application.port.outbound;

import com.finledger.ledger_service.adapter.outbound.persistence.LedgerSummaryView;
import com.finledger.ledger_service.domain.model.LedgerEntry;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface LedgerRepositoryPort {
    void saveAll(List<LedgerEntry> entries);
    List<LedgerEntry> find(UUID accountId, Instant from, Instant to);
    List<LedgerSummaryView> summaryByDate(Instant date);
}