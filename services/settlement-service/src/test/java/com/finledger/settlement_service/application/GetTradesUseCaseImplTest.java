package com.finledger.settlement_service.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.finledger.settlement_service.application.port.outbound.TradeRepositoryPort;
import com.finledger.settlement_service.application.service.GetTradesUseCaseImpl;
import com.finledger.settlement_service.domain.model.Trade;
import com.finledger.settlement_service.domain.value.Money;
import com.finledger.settlement_service.domain.value.Quantity;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GetTradesUseCaseImplTest {
  private TradeRepositoryPort repository;
  private GetTradesUseCaseImpl usecase;

  @BeforeEach
  void setUp() {
    repository = mock(TradeRepositoryPort.class);
    usecase = new GetTradesUseCaseImpl(repository);
  }

  @Test
  void execute_shouldReturnTradesFromRepository() {
    String symbol = "AAPL";
    Instant from = Instant.parse("2025-01-01T00:00:00Z");
    Instant to = Instant.parse("2025-01-31T23:59:59Z");
    Trade.Side side = Trade.Side.BUY;

    Trade trade1 =
        Trade.createNew(
            symbol,
            side,
            Quantity.of(100L),
            Money.of(BigDecimal.valueOf(150), Currency.getInstance("USD")),
            UUID.randomUUID(),
            UUID.randomUUID());

    Trade trade2 =
        Trade.createNew(
            symbol,
            side,
            Quantity.of(200L),
            Money.of(BigDecimal.valueOf(155), Currency.getInstance("USD")),
            UUID.randomUUID(),
            UUID.randomUUID());

    List<Trade> expected = List.of(trade1, trade2);

    when(repository.find(symbol, from, to, side)).thenReturn(expected);

    List<Trade> result = usecase.execute(symbol, from, to, side);

    assertThat(result).containsExactlyElementsOf(expected);
    verify(repository).find(symbol, from, to, side);
    verifyNoMoreInteractions(repository);
  }

  @Test
  void execute_shouldReturnEmptyListWhenRepositoryReturnsEmpty() {
    String symbol = "MSFT";
    Instant from = Instant.parse("2025-02-01T00:00:00Z");
    Instant to = Instant.parse("2025-02-28T23:59:59Z");
    Trade.Side side = Trade.Side.SELL;

    when(repository.find(symbol, from, to, side)).thenReturn(List.of());

    List<Trade> result = usecase.execute(symbol, from, to, side);

    assertThat(result).isEmpty();
    verify(repository).find(symbol, from, to, side);
  }
}
