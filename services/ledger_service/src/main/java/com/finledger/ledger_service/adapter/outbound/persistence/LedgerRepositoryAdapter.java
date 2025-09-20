package com.finledger.ledger_service.adapter.outbound.persistence;

import com.finledger.ledger_service.application.port.outbound.LedgerRepositoryPort;
import com.finledger.ledger_service.domain.model.LedgerEntry;
import com.finledger.ledger_service.domain.value.SignedMoney;
import jakarta.transaction.Transactional;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class LedgerRepositoryAdapter implements LedgerRepositoryPort {

  private final LedgerEntryJpaRepository repo;

  public LedgerRepositoryAdapter(LedgerEntryJpaRepository repo) {
    this.repo = repo;
  }

  @Override
  @Transactional
  public void saveAll(List<LedgerEntry> entries) {
    repo.saveAll(entries.stream().map(this::toEntity).toList());
  }

  @Override
  public List<LedgerEntry> find(UUID accountId, Instant from, Instant to) {
    return repo.search(accountId, from, to).stream().map(this::toDomain).toList();
  }

  @Override
  public List<LedgerSummaryView> summaryByDate(Instant date) {
    Instant datePlusOne =
        ZonedDateTime.ofInstant(date, ZoneId.of("Europe/London")).plusDays(1).toInstant();
    return repo.summaryByDate(date, datePlusOne);
  }

  private LedgerEntryEntity toEntity(LedgerEntry e) {
    LedgerEntryEntity le = new LedgerEntryEntity();
    le.setId(e.id());
    le.setEntryTime(e.entryTime());
    le.setAccountId(e.accountId());
    le.setCurrency(e.amount().currency().getCurrencyCode());
    le.setAmountSigned(e.amount().amount());
    le.setReferenceType(e.referenceType());
    le.setReferenceId(e.referenceId());
    le.setMessageId(e.messageId());
    return le;
  }

  private LedgerEntry toDomain(LedgerEntryEntity e) {
    return LedgerEntry.rehydrate(
        e.getId(),
        e.getEntryTime(),
        e.getAccountId(),
        SignedMoney.of(e.getAmountSigned(), e.getCurrency()),
        e.getReferenceType(),
        e.getReferenceId(),
        e.getMessageId());
  }
}
