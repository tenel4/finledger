package com.finledger.ledger_service.adapter.outbound.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface LedgerEntryJpaRepository extends JpaRepository<LedgerEntryEntity, UUID> {

    @Query("""
        SELECT e FROM LedgerEntryEntity e
        WHERE (:accountId IS NULL OR e.accountId = :accountId)
          AND (:from IS NULL OR e.entryTime >= :from)
          AND (:to IS NULL OR e.entryTime < :to)
        ORDER BY e.entryTime ASC
    """)
    List<LedgerEntryEntity> search(UUID accountId, Instant from, Instant to);

    @Query("""
    SELECT e.accountId AS accountId,
           e.currency AS currency,
           SUM(e.amountSigned) AS sum
    FROM LedgerEntryEntity e
    WHERE e.entryTime >= :date
      AND e.entryTime < :datePlusOne
    GROUP BY e.accountId, e.currency
    ORDER BY e.accountId, e.currency
""")
    List<LedgerSummaryView> summaryByDate(Instant date, Instant datePlusOne);
}
