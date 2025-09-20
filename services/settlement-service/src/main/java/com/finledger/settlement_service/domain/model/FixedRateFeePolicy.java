package com.finledger.settlement_service.domain.model;

import com.finledger.settlement_service.application.port.outbound.FeeRateRepositoryPort;
import com.finledger.settlement_service.domain.service.FeePolicy;
import com.finledger.settlement_service.domain.value.Money;
import java.math.BigDecimal;
import java.util.Objects;

public class FixedRateFeePolicy implements FeePolicy {
  private final FeeRateRepositoryPort feeRateRepositoryPort;
  private final String productCode;

  public FixedRateFeePolicy(FeeRateRepositoryPort feeRateRepositoryPort, String productCode) {
    this.feeRateRepositoryPort = feeRateRepositoryPort;
    this.productCode = productCode;
  }

  @Override
  public Money calculate(Money grossAmount) {
    Objects.requireNonNull(grossAmount);
    BigDecimal rate = feeRateRepositoryPort.getFeeRateForProduct(productCode);
    return grossAmount.times(rate);
  }
}
