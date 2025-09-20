package com.finledger.ledger_service.domain.value;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;
import java.util.Objects;

public final class Money {
  private static final int MAX_SCALE = 4;

  private final BigDecimal amount;
  private final Currency currency;

  private Money(BigDecimal amount, Currency currency) {
    this.amount = validateAmount(amount).setScale(MAX_SCALE, RoundingMode.HALF_UP);
    this.currency = Objects.requireNonNull(currency, "Money: currency cannot be null");
  }

  public static Money of(BigDecimal amount, String currency) {
    return new Money(amount, Currency.getInstance(currency));
  }

  public static Money of(BigDecimal amount, Currency currency) {
    return new Money(amount, currency);
  }

  public static Money zero(String currencyCode) {
    return new Money(BigDecimal.ZERO, Currency.getInstance(currencyCode));
  }

  public BigDecimal amount() {
    return amount;
  }

  public Currency currency() {
    return currency;
  }

  public Money add(Money other) {
    checkCurrency(other);
    return new Money(this.amount.add(other.amount), this.currency);
  }

  public Money subtract(Money other) {
    checkCurrency(other);
    BigDecimal result = this.amount.subtract(other.amount);
    if (result.compareTo(BigDecimal.ZERO) < 0) {
      throw new IllegalArgumentException("Resulting amount cannot be negative: " + result);
    }
    return new Money(result, this.currency);
  }

  public Money times(BigDecimal factor) {
    Objects.requireNonNull(factor, "Money: multiplication factor cannot be null");
    if (factor.compareTo(BigDecimal.ZERO) < 0) {
      throw new IllegalArgumentException("Money: multiplication factor cannot be negative");
    }
    BigDecimal result = this.amount.multiply(factor).setScale(MAX_SCALE, RoundingMode.HALF_UP);
    return new Money(result, this.currency);
  }

  private static BigDecimal validateAmount(BigDecimal amount) {
    Objects.requireNonNull(amount, "Money: amount cannot be null");
    if (amount.compareTo(BigDecimal.ZERO) < 0) {
      throw new IllegalArgumentException("Money: amount must be non-negative");
    }
    return amount;
  }

  private void checkCurrency(Money other) {
    if (!this.currency.equals(other.currency)) {
      throw new IllegalArgumentException(
          "Currency mismatch: " + this.currency + " vs " + other.currency);
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Money money = (Money) o;
    return amount.compareTo(money.amount) == 0 && currency.equals(money.currency);
  }

  @Override
  public int hashCode() {
    return Objects.hash(amount.stripTrailingZeros(), currency);
  }

  @Override
  public String toString() {
    return amount + " " + currency;
  }
}
