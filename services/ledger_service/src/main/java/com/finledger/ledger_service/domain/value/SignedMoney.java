package com.finledger.ledger_service.domain.value;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;
import java.util.Objects;

public final class SignedMoney {
  private static final int MAX_SCALE = 4;

  private final BigDecimal amount; // can be negative or positive
  private final Currency currency;

  private SignedMoney(BigDecimal amount, Currency currency) {
    this.amount =
        Objects.requireNonNull(amount, "SignedMoney: amount cannot be null")
            .setScale(MAX_SCALE, RoundingMode.HALF_UP);
    this.currency = Objects.requireNonNull(currency, "SignedMoney: currency cannot be null");
  }

  public static SignedMoney of(BigDecimal amount, String currencyCode) {
    return new SignedMoney(amount, Currency.getInstance(currencyCode));
  }

  public static SignedMoney of(BigDecimal amount, Currency currency) {
    return new SignedMoney(amount, currency);
  }

  public BigDecimal amount() {
    return amount;
  }

  public Currency currency() {
    return currency;
  }

  public boolean isDebit() {
    return amount.compareTo(BigDecimal.ZERO) < 0;
  }

  public boolean isCredit() {
    return amount.compareTo(BigDecimal.ZERO) > 0;
  }

  public Money absolute() {
    return Money.of(amount.abs(), currency);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof SignedMoney that)) return false;
    return amount.compareTo(that.amount) == 0 && currency.equals(that.currency);
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
