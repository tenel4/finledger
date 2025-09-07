package com.finledger.settlement_service.domain.model;

import java.math.BigDecimal;
import java.util.Objects;

public final class Money {
    private static final int MAX_SCALE = 4;

    private final BigDecimal amount;
    private final String currency;

    private Money(BigDecimal amount, String currency) {
        Objects.requireNonNull(amount, "Money: amount required");
        Objects.requireNonNull(currency, "Money: currency required");

        if (currency.isBlank()) throw new IllegalArgumentException("Money: currency must not be non-blank");
        if (amount.compareTo(BigDecimal.ZERO) < 0) throw new IllegalArgumentException("Money: amount must be non-negative");
        if (amount.scale() > MAX_SCALE) throw new IllegalArgumentException("Money: amount scale must be at most " + MAX_SCALE);

        this.amount = amount;
        this.currency = currency.toUpperCase();
    }

    public static Money of(BigDecimal amount, String currency) {
        return new Money(amount, currency);
    }

    public BigDecimal amount() { return amount; }
    public String currency() { return currency; }

    public Money plus(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException("Cannot add Money with different currencies: " + this.currency + " and " + other.currency);
        }
        return new Money(this.amount.add(other.amount), this.currency);
    }

    public Money minus(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException("Cannot subtract Money with different currencies: " + this.currency + " and " + other.currency);
        }
        return new Money(this.amount.subtract(other.amount), this.currency);
    }

    public Money times(BigDecimal factor) {
        return new Money(this.amount.multiply(factor), this.currency);
    }

    @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Money money = (Money) o;
            return amount.compareTo(money.amount) == 0 && currency.equals(money.currency);
        }

    @Override public int hashCode() { return Objects.hash(amount.stripTrailingZeros(), currency); }
    @Override public String toString() { return amount + " " + currency; }
}
