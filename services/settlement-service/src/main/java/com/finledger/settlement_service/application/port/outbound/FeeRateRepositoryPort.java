package com.finledger.settlement_service.application.port.outbound;

import java.math.BigDecimal;

public interface FeeRateRepositoryPort {
    BigDecimal getFeeRateForProduct(String productCode);
}
