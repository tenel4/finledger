package com.finledger.ledger_service.application.service;

import com.finledger.ledger_service.application.port.outbound.LedgerRepositoryPort;
import com.finledger.ledger_service.domain.model.LedgerEntry;
import com.finledger.ledger_service.domain.model.LedgerEntryReferenceType;
import com.finledger.ledger_service.domain.value.SignedMoney;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class GetLedgerEntriesUseCaseImplTest {

    private LedgerRepositoryPort ledgerRepo;
    private GetLedgerEntriesUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        ledgerRepo = mock(LedgerRepositoryPort.class);
        useCase = new GetLedgerEntriesUseCaseImpl(ledgerRepo);
    }

    @Test
    void execute_shouldDelegateToRepositoryAndReturnResults() {
        UUID accountId = UUID.randomUUID();
        Instant from = Instant.parse("2025-09-01T00:00:00Z");
        Instant to = Instant.parse("2025-09-10T00:00:00Z");

        LedgerEntry entry = LedgerEntry.rehydrate(
                UUID.randomUUID(),
                Instant.now(),
                accountId,
                SignedMoney.of(BigDecimal.TEN, Currency.getInstance("USD")),
                LedgerEntryReferenceType.TRADE,
                UUID.randomUUID(),
                UUID.randomUUID()
        );

        when(ledgerRepo.find(accountId, from, to)).thenReturn(List.of(entry));

        List<LedgerEntry> result = useCase.execute(accountId, from, to);

        assertThat(result).containsExactly(entry);
        verify(ledgerRepo, times(1)).find(accountId, from, to);
        verifyNoMoreInteractions(ledgerRepo);
    }
}
