package com.finledger.settlement_service.domain;

import com.finledger.settlement_service.domain.model.FixedRateFeePolicy;
import com.finledger.settlement_service.domain.value.Money;
import com.finledger.settlement_service.application.port.outbound.FeeRateRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.mock;

class FixedRateFeePolicyTest {
    private FeeRateRepositoryPort feeRateRepositoryPort;
    private final String productCode = "AAPL";

    @BeforeEach
    void setUp() {
        feeRateRepositoryPort = mock(FeeRateRepositoryPort.class);
    }

    @Test
    void calculate_shouldReturnGrossTimesRate() {
        Money gross = Money.of(BigDecimal.valueOf(1000), "USD");
        BigDecimal rate = BigDecimal.valueOf(0.05);
        when(feeRateRepositoryPort.getFeeRateForProduct(productCode)).thenReturn(rate);

        FixedRateFeePolicy policy = new FixedRateFeePolicy(feeRateRepositoryPort, productCode);

        Money fee = policy.calculate(gross);

        assertThat(fee.amount()).isEqualByComparingTo(BigDecimal.valueOf(50.0000));
        assertThat(fee.currency()).isEqualTo(gross.currency());
        verify(feeRateRepositoryPort).getFeeRateForProduct(productCode);
    }

    @Test
    void calculate_shouldReturnZero_whenRateIsZero() {
        Money gross = Money.of(BigDecimal.valueOf(1000), "USD");
        when(feeRateRepositoryPort.getFeeRateForProduct(productCode)).thenReturn(BigDecimal.ZERO);

        FixedRateFeePolicy policy = new FixedRateFeePolicy(feeRateRepositoryPort, productCode);

        Money fee = policy.calculate(gross);

        assertThat(fee.amount()).isEqualByComparingTo(BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP));
    }

    @Test
    void calculate_shouldReturnZero_whenGrossAmountIsZero() {
        Money gross = Money.of(BigDecimal.ZERO, "USD");
        when(feeRateRepositoryPort.getFeeRateForProduct(productCode)).thenReturn(BigDecimal.valueOf(0.10));

        FixedRateFeePolicy policy = new FixedRateFeePolicy(feeRateRepositoryPort, productCode);

        Money fee = policy.calculate(gross);

        assertThat(fee.amount()).isEqualByComparingTo(BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP));
    }

    @Test
    void calculate_shouldHandleNullRateGracefully() {
        Money gross = Money.of(BigDecimal.valueOf(500), "USD");
        when(feeRateRepositoryPort.getFeeRateForProduct(productCode)).thenReturn(null);

        FixedRateFeePolicy policy = new FixedRateFeePolicy(feeRateRepositoryPort, productCode);

        assertThrows(NullPointerException.class, () -> policy.calculate(gross));
    }
}
