package com.finledger.settlement_service.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.finledger.settlement_service.application.port.outbound.EodReconciliationPort;
import com.finledger.settlement_service.application.service.RunEodReconciliationUseCaseImpl;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RunEodReconciliationUseCaseImplTest {
  private EodReconciliationPort eodReconciliationPort;
  private RunEodReconciliationUseCaseImpl useCase;

  @BeforeEach
  void setUp() {
    eodReconciliationPort = mock(EodReconciliationPort.class);
    useCase = new RunEodReconciliationUseCaseImpl(eodReconciliationPort);
  }

  @Test
  void execute_shouldDelegateToPortAndReturnResult() {
    // Given
    LocalDate date = LocalDate.of(2025, 9, 10);
    EodReconciliationPort.Result expectedResult =
        new EodReconciliationPort.Result("/var/finledger/reports", 3);

    when(eodReconciliationPort.runAndExport(date)).thenReturn(expectedResult);

    // When
    EodReconciliationPort.Result actualResult = useCase.execute(date);

    // Then
    assertThat(actualResult).isSameAs(expectedResult);
    verify(eodReconciliationPort).runAndExport(date);
    verifyNoMoreInteractions(eodReconciliationPort);
  }
}
