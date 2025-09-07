package com.finledger.settlement_service.domain.model;

public final class Quantity {
    private final long value;

    private Quantity(long value) {
        if (value <= 0) { throw new IllegalArgumentException("Quantity must be positive"); }
        this.value = value;
    }

    public static Quantity of(long value) { return new Quantity(value); }
    public long value() { return value; }
}
