package com.finledger.ledger_service.application.service;

import com.finledger.ledger_service.adapter.outbound.persistence.LedgerSummaryView;
import com.finledger.ledger_service.application.port.inbound.GetLedgerSummaryUseCase;
import com.finledger.ledger_service.application.port.outbound.LedgerRepositoryPort;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class GetLedgerSummaryUseCaseImpl implements GetLedgerSummaryUseCase {
    private final LedgerRepositoryPort ledgerRepo;

    public GetLedgerSummaryUseCaseImpl(LedgerRepositoryPort ledgerRepo) {
        this.ledgerRepo = ledgerRepo;
    }

    @Override
    public List<Result> execute(Instant date) {
        List<LedgerSummaryView> view = ledgerRepo.summaryByDate(date);
        return view.stream().map(v ->
            new Result(v.getAccountId(), v.getCurrency(), v.getSum())
        ).toList();
    }
}
