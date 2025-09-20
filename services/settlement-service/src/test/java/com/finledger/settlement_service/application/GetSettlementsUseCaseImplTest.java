package com.finledger.settlement_service.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.finledger.settlement_service.application.port.outbound.SettlementRepositoryPort;
import com.finledger.settlement_service.application.service.GetSettlementsUseCaseImpl;
import com.finledger.settlement_service.domain.model.Settlement;
import com.finledger.settlement_service.domain.value.Money;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GetSettlementsUseCaseImplTest {
  private SettlementRepositoryPort repository;
  private GetSettlementsUseCaseImpl useCase;

  @BeforeEach
  void setUp() {
    repository = mock(SettlementRepositoryPort.class);
    useCase = new GetSettlementsUseCaseImpl(repository);
  }

  @Test
  void execute_shouldReturnSettlementsFromRepository() {
    LocalDate date = LocalDate.now();
    Settlement.Status status = Settlement.Status.PENDING;

    Settlement settlement1 =
        Settlement.createNew(
            UUID.randomUUID(),
            date,
            Money.of(BigDecimal.valueOf(1000), Currency.getInstance("USD")),
            status,
            UUID.randomUUID());

    Settlement settlement2 =
        Settlement.createNew(
            UUID.randomUUID(),
            date,
            Money.of(BigDecimal.valueOf(2000), Currency.getInstance("USD")),
            status,
            UUID.randomUUID());

    List<Settlement> expected = List.of(settlement1, settlement2);

    when(repository.find(status, date)).thenReturn(expected);

    List<Settlement> result = useCase.execute(status, date);

    assertThat(result).containsExactlyElementsOf(expected);
    verify(repository).find(status, date);
    verifyNoMoreInteractions(repository);
  }

  @Test
  void execute_shouldReturnEmptyListWhenRepositoryReturnsEmpty() {
    LocalDate date = LocalDate.now();
    Settlement.Status status = Settlement.Status.SETTLED;

    when(repository.find(status, date)).thenReturn(List.of());

    List<Settlement> result = useCase.execute(status, date);

    assertThat(result).isEmpty();
    verify(repository).find(status, date);
  }
}
