package com.finledger.ledger_service.application.port.inbound;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface GetLedgerSummaryUseCase {
    List<Result> execute(Instant date);

    record Result(UUID accountId, String currency, java.math.BigDecimal sum) {}
}
