package com.finledger.ledger_service.util;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

public final class DateConversionUtil {
    private DateConversionUtil() {}

    public static Instant startOfDay(LocalDate d, ZoneId zone) {
        return d == null ? null : d.atStartOfDay(zone).toInstant();
    }

    public static Instant startOfNextDay(LocalDate d, ZoneId zone) {
        return d == null ? null : d.plusDays(1).atStartOfDay(zone).toInstant();
    }
}
