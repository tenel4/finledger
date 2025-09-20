package com.finledger.settlement_service.adapter.outbound.persistence.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.finledger.settlement_service.adapter.outbound.persistance.repository.SettlementRepositoryAdapter;
import com.finledger.settlement_service.domain.model.Settlement;
import com.finledger.settlement_service.domain.value.Money;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@DataJpaTest
@Import(SettlementRepositoryAdapter.class) // bring in the adapter bean
class SettlementRepositoryAdapterIT {

  @Container
  static PostgreSQLContainer<?> postgres =
      new PostgreSQLContainer<>("postgres:16-alpine")
          .withDatabaseName("testdb")
          .withUsername("test")
          .withPassword("test");

  @DynamicPropertySource
  static void configureProps(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", postgres::getJdbcUrl);
    registry.add("spring.datasource.username", postgres::getUsername);
    registry.add("spring.datasource.password", postgres::getPassword);
    registry.add("spring.jpa.hibernate.ddl-auto", () -> "update");
  }

  @Autowired private SettlementRepositoryAdapter adapter;

  @Test
  void saveAndFind_shouldPersistAndRetrieveByStatusAndDate() {
    UUID settlementId = UUID.randomUUID();
    UUID tradeId = UUID.randomUUID();
    UUID messageId = UUID.randomUUID();
    LocalDate valueDate = LocalDate.of(2025, 9, 10);

    Money gross = Money.of(BigDecimal.valueOf(1000), "USD");
    Money fees = Money.of(BigDecimal.valueOf(50), "USD");
    Money net = Money.of(BigDecimal.valueOf(950), "USD");

    Settlement settlement =
        Settlement.rehydrate(
            settlementId,
            tradeId,
            valueDate,
            gross,
            fees,
            net,
            Settlement.Status.PENDING,
            messageId);

    // Save
    adapter.save(settlement);

    // Find by status and date
    List<Settlement> results = adapter.find(Settlement.Status.PENDING, valueDate);

    assertThat(results).hasSize(1);
    Settlement found = results.getFirst();
    assertThat(found.id()).isEqualTo(settlementId);
    assertThat(found.tradeId()).isEqualTo(tradeId);
    assertThat(found.valueDate()).isEqualTo(valueDate);
    assertThat(found.grossAmount()).isEqualTo(gross);
    assertThat(found.fees()).isEqualTo(fees);
    assertThat(found.netAmount()).isEqualTo(net);
    assertThat(found.status()).isEqualTo(Settlement.Status.PENDING);
    assertThat(found.messageId()).isEqualTo(messageId);
  }

  @Test
  void find_shouldReturnEmptyList_whenNoMatch() {
    List<Settlement> results = adapter.find(Settlement.Status.SETTLED, LocalDate.now());
    assertThat(results).isEmpty();
  }
}
