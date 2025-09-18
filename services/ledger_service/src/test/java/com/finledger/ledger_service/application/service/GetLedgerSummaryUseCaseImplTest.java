package com.finledger.ledger_service.application.service;

import com.finledger.ledger_service.adapter.outbound.persistence.LedgerSummaryView;
import com.finledger.ledger_service.application.port.inbound.GetLedgerSummaryUseCase;
import com.finledger.ledger_service.application.port.outbound.LedgerRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class GetLedgerSummaryUseCaseImplTest {

    private LedgerRepositoryPort ledgerRepo;
    private GetLedgerSummaryUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        ledgerRepo = mock(LedgerRepositoryPort.class);
        useCase = new GetLedgerSummaryUseCaseImpl(ledgerRepo);
    }

    @Test
    void execute_shouldDelegateToRepositoryAndMapResults() {
        Instant date = Instant.parse("2025-09-18T00:00:00Z");

        UUID accountId1 = UUID.randomUUID();
        UUID accountId2 = UUID.randomUUID();

        LedgerSummaryView view1 = new TestLedgerSummaryView(accountId1, Currency.getInstance("USD").toString(), BigDecimal.valueOf(100.25));
        LedgerSummaryView view2 = new TestLedgerSummaryView(accountId2, Currency.getInstance("EUR").toString(), BigDecimal.valueOf(-50.75));

        when(ledgerRepo.summaryByDate(date)).thenReturn(List.of(view1, view2));

        List<GetLedgerSummaryUseCase.Result> results = useCase.execute(date);

        assertThat(results).hasSize(2);

        GetLedgerSummaryUseCase.Result r1 = results.getFirst();
        assertThat(r1.accountId()).isEqualTo(accountId1);
        assertThat(r1.currency()).isEqualTo(Currency.getInstance("USD").toString());
        assertThat(r1.sum()).isEqualByComparingTo("100.25");

        GetLedgerSummaryUseCase.Result r2 = results.get(1);
        assertThat(r2.accountId()).isEqualTo(accountId2);
        assertThat(r2.currency()).isEqualTo(Currency.getInstance("EUR").toString());
        assertThat(r2.sum()).isEqualByComparingTo("-50.75");

        verify(ledgerRepo, times(1)).summaryByDate(date);
        verifyNoMoreInteractions(ledgerRepo);
    }

    /**
     * Simple test stub for LedgerSummaryView.
     */
    private static class TestLedgerSummaryView implements LedgerSummaryView {
        private final UUID accountId;
        private final String currency;
        private final BigDecimal sum;

        TestLedgerSummaryView(UUID accountId, String currency, BigDecimal sum) {
            this.accountId = accountId;
            this.currency = currency;
            this.sum = sum;
        }

        @Override
        public UUID getAccountId() {
            return accountId;
        }

        @Override
        public String getCurrency() {
            return currency;
        }

        @Override
        public BigDecimal getSum() {
            return sum;
        }
    }
}
