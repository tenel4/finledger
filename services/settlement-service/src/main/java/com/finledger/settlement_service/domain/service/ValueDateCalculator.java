package com.finledger.settlement_service.domain.service;

import java.time.Instant;
import java.time.LocalDate;

public interface ValueDateCalculator {
    LocalDate calculate(Instant tradeTime);
}
