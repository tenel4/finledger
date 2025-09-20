package com.finledger.ledger_service.domain.value;

import static org.assertj.core.api.Assertions.*;

import java.math.BigDecimal;
import java.util.Currency;
import org.junit.jupiter.api.Test;

class MoneyTest {

  @Test
  void of_withStringCurrency_setsAmountAndCurrencyWithScale() {
    Money money = Money.of(BigDecimal.valueOf(10.123456), "USD");
    assertThat(money.amount()).isEqualByComparingTo("10.1235"); // rounded to 4 dp
    assertThat(money.currency()).isEqualTo(Currency.getInstance("USD"));
  }

  @Test
  void of_withCurrency_setsAmountAndCurrencyWithScale() {
    Currency eur = Currency.getInstance("EUR");
    Money money = Money.of(BigDecimal.valueOf(5), eur);
    assertThat(money.amount()).isEqualByComparingTo("5.0000");
    assertThat(money.currency()).isEqualTo(eur);
  }

  @Test
  void zero_returnsZeroAmountWithGivenCurrency() {
    Money zero = Money.zero("GBP");
    assertThat(zero.amount()).isEqualByComparingTo("0.0000");
    assertThat(zero.currency()).isEqualTo(Currency.getInstance("GBP"));
  }

  @Test
  void add_withSameCurrency_returnsSum() {
    Money m1 = Money.of(BigDecimal.valueOf(10), "USD");
    Money m2 = Money.of(BigDecimal.valueOf(5.25), "USD");
    Money sum = m1.add(m2);
    assertThat(sum.amount()).isEqualByComparingTo("15.2500");
    assertThat(sum.currency()).isEqualTo(Currency.getInstance("USD"));
  }

  @Test
  void add_withDifferentCurrency_throwsException() {
    Money usd = Money.of(BigDecimal.ONE, "USD");
    Money eur = Money.of(BigDecimal.ONE, "EUR");
    assertThatThrownBy(() -> usd.add(eur))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Currency mismatch");
  }

  @Test
  void subtract_withSameCurrency_returnsDifference() {
    Money m1 = Money.of(BigDecimal.valueOf(10), "USD");
    Money m2 = Money.of(BigDecimal.valueOf(4.5), "USD");
    Money diff = m1.subtract(m2);
    assertThat(diff.amount()).isEqualByComparingTo("5.5000");
  }

  @Test
  void subtract_resultingNegative_throwsException() {
    Money m1 = Money.of(BigDecimal.valueOf(5), "USD");
    Money m2 = Money.of(BigDecimal.valueOf(10), "USD");
    assertThatThrownBy(() -> m1.subtract(m2))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Resulting amount cannot be negative");
  }

  @Test
  void subtract_withDifferentCurrency_throwsException() {
    Money usd = Money.of(BigDecimal.ONE, "USD");
    Money eur = Money.of(BigDecimal.ONE, "EUR");
    assertThatThrownBy(() -> usd.subtract(eur))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Currency mismatch");
  }

  @Test
  void times_withPositiveFactor_returnsScaledProduct() {
    Money m = Money.of(BigDecimal.valueOf(2.5), "USD");
    Money result = m.times(BigDecimal.valueOf(3));
    assertThat(result.amount()).isEqualByComparingTo("7.5000");
    assertThat(result.currency()).isEqualTo(m.currency());
  }

  @Test
  void times_withNullFactor_throwsException() {
    Money m = Money.of(BigDecimal.ONE, "USD");
    assertThatThrownBy(() -> m.times(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessageContaining("multiplication factor cannot be null");
  }

  @Test
  void times_withNegativeFactor_throwsException() {
    Money m = Money.of(BigDecimal.ONE, "USD");
    assertThatThrownBy(() -> m.times(BigDecimal.valueOf(-1)))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("multiplication factor cannot be negative");
  }

  @Test
  void of_withNullAmount_throwsException() {
    assertThatThrownBy(() -> Money.of(null, "USD"))
        .isInstanceOf(NullPointerException.class)
        .hasMessageContaining("amount cannot be null");
  }

  @Test
  void of_withNegativeAmount_throwsException() {
    assertThatThrownBy(() -> Money.of(BigDecimal.valueOf(-1), "USD"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("amount must be non-negative");
  }

  @Test
  void equalsAndHashCode_considerValueAndCurrencyOnly() {
    Money m1 = Money.of(BigDecimal.valueOf(10.0), "USD");
    Money m2 = Money.of(BigDecimal.valueOf(10.0000), "USD");
    Money m3 = Money.of(BigDecimal.valueOf(11), "USD");

    assertThat(m1).isEqualTo(m2).hasSameHashCodeAs(m2).isNotEqualTo(m3);
  }

  @Test
  void toString_containsAmountAndCurrency() {
    Money m = Money.of(BigDecimal.valueOf(12.3456), "USD");
    assertThat(m.toString()).hasToString("12.3456 USD");
  }
}
