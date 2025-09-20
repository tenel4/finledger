package com.finledger.settlement_service.adapter.service;

import com.finledger.settlement_service.domain.service.ValueDateCalculator;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
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
        LocalDate date = tradeTime.atZone(marketZone).toLocalDate();
        int added = 0;

        while (added < settlementDays) {
            date = date.plusDays(1);
            if (!isWeekend(date)) {
                added++;
            }
        }

        return date;
    }

    private boolean isWeekend(LocalDate date) {
        DayOfWeek dow = date.getDayOfWeek();
        return dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY;
    }
}
