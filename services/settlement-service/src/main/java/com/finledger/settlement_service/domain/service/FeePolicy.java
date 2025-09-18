package com.finledger.settlement_service.domain.service;

import com.finledger.settlement_service.domain.value.Money;

public interface FeePolicy {
    Money calculate(Money grossAmount);
}
