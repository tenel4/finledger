package com.finledger.ledger_service.adapter.outbound.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.finledger.ledger_service.domain.model.LedgerEntry;
import com.finledger.ledger_service.domain.model.LedgerEntryReferenceType;
import com.finledger.ledger_service.domain.value.SignedMoney;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers
class LedgerRepositoryAdapterIT {

  @Container
  static PostgreSQLContainer<?> postgres =
      new PostgreSQLContainer<>("postgres:16-alpine")
          .withDatabaseName("ledger_test")
          .withUsername("test")
          .withPassword("test");

  @DynamicPropertySource
  static void configureProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", postgres::getJdbcUrl);
    registry.add("spring.datasource.username", postgres::getUsername);
    registry.add("spring.datasource.password", postgres::getPassword);
  }

  @Autowired private LedgerRepositoryAdapter adapter;

  @Test
  void saveAll_and_find_shouldWorkWithPostgres() {
    UUID accountId = UUID.randomUUID();
    Instant now = Instant.now();

    LedgerEntry entry =
        LedgerEntry.rehydrate(
            UUID.randomUUID(),
            now,
            accountId,
            SignedMoney.of(BigDecimal.valueOf(150), Currency.getInstance("USD")),
            LedgerEntryReferenceType.SETTLEMENT,
            UUID.randomUUID(),
            UUID.randomUUID());

    adapter.saveAll(List.of(entry));

    List<LedgerEntry> results = adapter.find(accountId, now.minusSeconds(60), now.plusSeconds(60));

    assertThat(results).hasSize(1);
    LedgerEntry retrieved = results.getFirst();
    assertThat(retrieved.accountId()).isEqualTo(accountId);
    assertThat(retrieved.amount().amount()).isEqualByComparingTo("150");
  }

  @Test
  void summaryByDate_shouldReturnAggregatedResults() {
    UUID accountId = UUID.randomUUID();
    Instant now = Instant.now();

    LedgerEntry entry =
        LedgerEntry.rehydrate(
            UUID.randomUUID(),
            now,
            accountId,
            SignedMoney.of(BigDecimal.valueOf(200), Currency.getInstance("USD")),
            LedgerEntryReferenceType.SETTLEMENT,
            UUID.randomUUID(),
            UUID.randomUUID());

    adapter.saveAll(List.of(entry));

    List<LedgerSummaryView> summaries = adapter.summaryByDate(now);

    assertThat(summaries).isNotEmpty();
    // Add field-specific assertions depending on LedgerSummaryView
  }
}
