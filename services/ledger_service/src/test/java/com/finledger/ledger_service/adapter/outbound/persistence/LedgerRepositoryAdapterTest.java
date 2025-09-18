package com.finledger.ledger_service.adapter.outbound.persistence;

import com.finledger.ledger_service.domain.model.LedgerEntry;
import com.finledger.ledger_service.domain.model.LedgerEntryReferenceType;
import com.finledger.ledger_service.domain.value.SignedMoney;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.*;
import java.util.Currency;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class LedgerRepositoryAdapterTest {

    private LedgerEntryJpaRepository repo;
    private LedgerRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        repo = mock(LedgerEntryJpaRepository.class);
        adapter = new LedgerRepositoryAdapter(repo);
    }

    @Test
    void saveAll_shouldMapDomainToEntityAndCallRepo() {
        LedgerEntry entry = LedgerEntry.rehydrate(
                UUID.randomUUID(),
                Instant.parse("2025-09-18T10:15:30Z"),
                UUID.randomUUID(),
                SignedMoney.of(BigDecimal.valueOf(123.45), Currency.getInstance("USD")),
                LedgerEntryReferenceType.TRADE,
                UUID.randomUUID(),
                UUID.randomUUID()
        );

        adapter.saveAll(List.of(entry));

        verify(repo, times(1)).saveAll(argThat(iterable -> {
            List<LedgerEntryEntity> entities = (List<LedgerEntryEntity>) iterable;
            assertThat(entities).hasSize(1);
            LedgerEntryEntity e = entities.getFirst();
            assertThat(e.getId()).isEqualTo(entry.id());
            assertThat(e.getEntryTime()).isEqualTo(entry.entryTime());
            assertThat(e.getAccountId()).isEqualTo(entry.accountId());
            assertThat(e.getCurrency()).isEqualTo("USD");
            assertThat(e.getAmountSigned()).isEqualByComparingTo("123.45");
            assertThat(e.getReferenceType()).isEqualTo(entry.referenceType());
            assertThat(e.getReferenceId()).isEqualTo(entry.referenceId());
            assertThat(e.getMessageId()).isEqualTo(entry.messageId());
            return true;
        }));
    }

    @Test
    void find_shouldMapEntitiesToDomainObjects() {
        LedgerEntryEntity entity = new LedgerEntryEntity();
        UUID id = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        UUID referenceId = UUID.randomUUID();
        UUID messageId = UUID.randomUUID();
        Instant entryTime = Instant.parse("2025-09-18T10:15:30Z");

        entity.setId(id);
        entity.setEntryTime(entryTime);
        entity.setAccountId(accountId);
        entity.setCurrency("USD");
        entity.setAmountSigned(BigDecimal.valueOf(50.00));
        entity.setReferenceType(LedgerEntryReferenceType.SETTLEMENT);
        entity.setReferenceId(referenceId);
        entity.setMessageId(messageId);

        when(repo.search(any(), any(), any())).thenReturn(List.of(entity));

        List<LedgerEntry> result = adapter.find(accountId, Instant.now(), Instant.now().plusSeconds(3600));

        assertThat(result).hasSize(1);
        LedgerEntry entry = result.getFirst();
        assertThat(entry.id()).isEqualTo(id);
        assertThat(entry.entryTime()).isEqualTo(entryTime);
        assertThat(entry.accountId()).isEqualTo(accountId);
        assertThat(entry.amount().currency()).isEqualTo(Currency.getInstance("USD"));
        assertThat(entry.amount().amount()).isEqualByComparingTo("50.00");
        assertThat(entry.referenceType()).isEqualTo(LedgerEntryReferenceType.SETTLEMENT);
        assertThat(entry.referenceId()).isEqualTo(referenceId);
        assertThat(entry.messageId()).isEqualTo(messageId);
    }

    @Test
    void summaryByDate_shouldPassCorrectDateRangeToRepo() {
        Instant date = Instant.parse("2025-09-18T00:00:00Z");
        Instant expectedPlusOne = ZonedDateTime.ofInstant(date, ZoneId.of("Europe/London"))
                .plusDays(1)
                .toInstant();

        LedgerSummaryView view = mock(LedgerSummaryView.class);
        when(repo.summaryByDate(any(), any())).thenReturn(List.of(view));

        List<LedgerSummaryView> result = adapter.summaryByDate(date);

        assertThat(result).containsExactly(view);
        verify(repo).summaryByDate(date, expectedPlusOne);
    }
}
