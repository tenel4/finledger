package com.finledger.settlement_service.adapter.inbound.scheduler;

import com.finledger.settlement_service.application.port.inbound.RunEodReconciliationUseCase;
import com.finledger.settlement_service.application.port.outbound.EodReconciliationPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.ZoneId;

import static org.mockito.Mockito.*;

class EodSchedulerTest {

    private RunEodReconciliationUseCase runEod;
    private EodScheduler scheduler;

    @BeforeEach
    void setUp() {
        runEod = mock(RunEodReconciliationUseCase.class);
        scheduler = new EodScheduler(runEod);
    }

    @Test
    void runEodJob_shouldCallUseCaseWithTodayUtcDate() {
        // Arrange
        LocalDate todayUtc = LocalDate.now(ZoneId.of("UTC"));
        EodReconciliationPort.Result fakeResult =
                new EodReconciliationPort.Result("/tmp/report", 5);
        when(runEod.execute(any(LocalDate.class))).thenReturn(fakeResult);

        // Act
        scheduler.runEodJob();

        // Assert
        verify(runEod).execute(todayUtc);
        // We don't assert logs here, but could with a log appender if needed
    }
}
