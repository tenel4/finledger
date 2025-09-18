package com.finledger.settlement_service.adapter.service;

import com.finledger.settlement_service.domain.service.ValueDateCalculator;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

@Component
public class SimpleValueDateCalculator implements ValueDateCalculator {

    private final ZoneId marketZone;
    private final int settlementDays;

    public SimpleValueDateCalculator() {
        this.marketZone = ZoneId.of("Europe/London");
        this.settlementDays = 2;
    }

    @Override
    public LocalDate calculate(Instant tradeTime) {
        return tradeTime
                .atZone(marketZone)
                .toLocalDate()
                .plusDays(settlementDays);
    }
}
