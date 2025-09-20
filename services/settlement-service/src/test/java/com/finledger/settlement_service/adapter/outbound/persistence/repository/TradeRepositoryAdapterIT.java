package com.finledger.settlement_service.adapter.outbound.persistence.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.finledger.settlement_service.adapter.outbound.persistance.repository.TradeRepositoryAdapter;
import com.finledger.settlement_service.domain.model.Trade;
import com.finledger.settlement_service.domain.value.Money;
import com.finledger.settlement_service.domain.value.Quantity;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
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
@Import(TradeRepositoryAdapter.class) // bring in the adapter bean
class TradeRepositoryAdapterIT {

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

  @Autowired private TradeRepositoryAdapter adapter;

  @Test
  void saveAndFindById_shouldPersistAndRetrieveTrade() {
    UUID tradeId = UUID.randomUUID();
    UUID buyerId = UUID.randomUUID();
    UUID sellerId = UUID.randomUUID();
    Instant tradeTime = Instant.parse("2025-09-10T10:15:30Z");

    Trade trade =
        Trade.rehydrate(
            tradeId,
            "AAPL",
            Trade.Side.BUY,
            Quantity.of(10),
            Money.of(BigDecimal.valueOf(150.25), "USD"),
            buyerId,
            sellerId,
            tradeTime);

    // Save
    adapter.save(trade);

    // Retrieve by ID
    Optional<Trade> foundOpt = adapter.findById(tradeId);
    assertThat(foundOpt).isPresent();
    Trade found = foundOpt.get();

    assertThat(found.id()).isEqualTo(tradeId);
    assertThat(found.symbol()).isEqualTo("AAPL");
    assertThat(found.side()).isEqualTo(Trade.Side.BUY);
    assertThat(found.quantity()).isEqualTo(Quantity.of(10));
    assertThat(found.price()).isEqualTo(Money.of(BigDecimal.valueOf(150.25), "USD"));
    assertThat(found.buyerAccountId()).isEqualTo(buyerId);
    assertThat(found.sellerAccountId()).isEqualTo(sellerId);
    assertThat(found.tradeTime()).isEqualTo(tradeTime);
  }

  @Test
  void find_shouldReturnTradesMatchingCriteria() {
    UUID buyerId = UUID.randomUUID();
    UUID sellerId = UUID.randomUUID();
    Instant now = Instant.now();

    Trade trade1 =
        Trade.rehydrate(
            UUID.randomUUID(),
            "AAPL",
            Trade.Side.BUY,
            Quantity.of(5),
            Money.of(BigDecimal.valueOf(100), "USD"),
            buyerId,
            sellerId,
            now.minusSeconds(3600));

    Trade trade2 =
        Trade.rehydrate(
            UUID.randomUUID(),
            "AAPL",
            Trade.Side.SELL,
            Quantity.of(8),
            Money.of(BigDecimal.valueOf(200), "USD"),
            buyerId,
            sellerId,
            now.minusSeconds(1800));

    adapter.save(trade1);
    adapter.save(trade2);

    // Search for BUY trades in the last 2 hours
    List<Trade> results = adapter.find("AAPL", now.minusSeconds(7200), now, Trade.Side.BUY);

    assertThat(results).hasSize(1);
    assertThat(results.getFirst().side()).isEqualTo(Trade.Side.BUY);
    assertThat(results.getFirst().symbol()).isEqualTo("AAPL");
  }
}
