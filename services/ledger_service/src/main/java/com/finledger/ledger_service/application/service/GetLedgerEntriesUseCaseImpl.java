package com.finledger.ledger_service.application.service;

import com.finledger.ledger_service.application.port.inbound.GetLedgerEntriesUseCase;
import com.finledger.ledger_service.application.port.outbound.LedgerRepositoryPort;
import com.finledger.ledger_service.domain.model.LedgerEntry;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class GetLedgerEntriesUseCaseImpl implements GetLedgerEntriesUseCase {
  private final LedgerRepositoryPort ledgerRepo;

  public GetLedgerEntriesUseCaseImpl(LedgerRepositoryPort ledgerRepo) {
    this.ledgerRepo = ledgerRepo;
  }

  @Override
  public List<LedgerEntry> execute(UUID accountId, Instant from, Instant to) {
    return ledgerRepo.find(accountId, from, to);
  }
}
