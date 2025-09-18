package com.finledger.settlement_service.domain;

import com.finledger.settlement_service.domain.value.Money;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;

import static org.assertj.core.api.Assertions.*;

class MoneyTest {

    @Test
    void of_shouldCreateMoneyWithRoundedScale() {
        Money money = Money.of(new BigDecimal("10.123456"), "USD");
        assertThat(money.amount()).isEqualByComparingTo(new BigDecimal("10.1235"));
        assertThat(money.currency()).isEqualTo(Currency.getInstance("USD"));
    }

    @Test
    void of_shouldThrowException_whenAmountIsNull() {
        assertThatThrownBy(() -> Money.of(null, "USD"))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Money: amount cannot be null");
    }

    @Test
    void of_shouldThrowException_whenCurrencyIsNull() {
        assertThatThrownBy(() -> Money.of(BigDecimal.ONE, (String) null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Money: currency cannot be null");
    }

    @Test
    void of_shouldThrowException_whenAmountIsNegative() {
        BigDecimal amount = BigDecimal.valueOf(-1);
        assertThatThrownBy(() -> Money.of(amount, "USD"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Money: amount must be non-negative");
    }

    @Test
    void add_shouldReturnSum_whenCurrenciesMatch() {
        Money m1 = Money.of(BigDecimal.valueOf(10), "USD");
        Money m2 = Money.of(BigDecimal.valueOf(5), "USD");

        Money result = m1.add(m2);

        assertThat(result.amount()).isEqualByComparingTo(BigDecimal.valueOf(15).setScale(4, RoundingMode.HALF_UP));
        assertThat(result.currency()).isEqualTo(Currency.getInstance("USD"));
    }

    @Test
    void add_shouldThrowException_whenCurrenciesDiffer() {
        Money usd = Money.of(BigDecimal.TEN, "USD");
        Money eur = Money.of(BigDecimal.ONE, "EUR");

        assertThatThrownBy(() -> usd.add(eur))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Currency mismatch");
    }

    @Test
    void subtract_shouldReturnDifference_whenCurrenciesMatch() {
        Money m1 = Money.of(BigDecimal.valueOf(10), "USD");
        Money m2 = Money.of(BigDecimal.valueOf(4), "USD");

        Money result = m1.subtract(m2);

        assertThat(result.amount()).isEqualByComparingTo(BigDecimal.valueOf(6).setScale(4, RoundingMode.HALF_UP));
        assertThat(result.currency()).isEqualTo(Currency.getInstance("USD"));
    }

    @Test
    void subtract_shouldThrowException_whenCurrenciesDiffer() {
        Money usd = Money.of(BigDecimal.TEN, "USD");
        Money eur = Money.of(BigDecimal.ONE, "EUR");

        assertThatThrownBy(() -> usd.subtract(eur))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Currency mismatch");
    }

    @Test
    void times_shouldMultiplyAmount() {
        Money m = Money.of(BigDecimal.valueOf(10), "USD");

        Money result = m.times(BigDecimal.valueOf(2.5));

        assertThat(result.amount()).isEqualByComparingTo(BigDecimal.valueOf(25).setScale(4, RoundingMode.HALF_UP));
        assertThat(result.currency()).isEqualTo(Currency.getInstance("USD"));
    }

    @Test
    void equalsAndHashCode_shouldWorkForSameValueAndCurrency() {
        Money m1 = Money.of(BigDecimal.valueOf(10.00), "USD");
        Money m2 = Money.of(BigDecimal.valueOf(10.0000), "USD");

        assertThat(m1)
                .isEqualTo(m2)
                .hasSameHashCodeAs(m2);
    }

    @Test
    void toString_shouldReturnFormattedString() {
        Money m = Money.of(BigDecimal.valueOf(10), "USD");
        assertThat(m.toString()).hasToString("10.0000 USD");
    }
}
