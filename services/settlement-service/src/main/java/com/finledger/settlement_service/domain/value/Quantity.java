package com.finledger.settlement_service.domain.value;

import java.util.Objects;

public final class Quantity {
    private final long value;

    private Quantity(long value) {
        if (value <= 0) { throw new IllegalArgumentException("Quantity must be greater than zero, but was: " + value); }
        this.value = value;
    }

    public static Quantity of(long value) { return new Quantity(value); }
    public long value() { return value; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Quantity quantity)) return false;
        return value == quantity.value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return "Quantity{" + "value=" + value + '}';
    }
}
