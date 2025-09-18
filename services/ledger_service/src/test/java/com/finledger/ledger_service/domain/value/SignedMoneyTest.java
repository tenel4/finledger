package com.finledger.ledger_service.domain.value;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Currency;

import static org.assertj.core.api.Assertions.*;

class SignedMoneyTest {

    @Test
    void of_withStringCurrency_setsAmountAndCurrencyWithScale() {
        SignedMoney sm = SignedMoney.of(BigDecimal.valueOf(10.123456), "USD");
        assertThat(sm.amount()).isEqualByComparingTo("10.1235"); // rounded to 4 dp
        assertThat(sm.currency()).isEqualTo(Currency.getInstance("USD"));
    }

    @Test
    void of_withCurrency_setsAmountAndCurrencyWithScale() {
        Currency eur = Currency.getInstance("EUR");
        SignedMoney sm = SignedMoney.of(BigDecimal.valueOf(-5), eur);
        assertThat(sm.amount()).isEqualByComparingTo("-5.0000");
        assertThat(sm.currency()).isEqualTo(eur);
    }

    @Test
    void isDebit_returnsTrueForNegativeAmount() {
        SignedMoney sm = SignedMoney.of(BigDecimal.valueOf(-1), "USD");
        assertThat(sm.isDebit()).isTrue();
        assertThat(sm.isCredit()).isFalse();
    }

    @Test
    void isCredit_returnsTrueForPositiveAmount() {
        SignedMoney sm = SignedMoney.of(BigDecimal.valueOf(1), "USD");
        assertThat(sm.isCredit()).isTrue();
        assertThat(sm.isDebit()).isFalse();
    }

    @Test
    void isDebitAndCredit_falseForZeroAmount() {
        SignedMoney sm = SignedMoney.of(BigDecimal.ZERO, "USD");
        assertThat(sm.isDebit()).isFalse();
        assertThat(sm.isCredit()).isFalse();
    }

    @Test
    void absolute_returnsMoneyWithPositiveAmountAndSameCurrency() {
        SignedMoney sm = SignedMoney.of(BigDecimal.valueOf(-12.3456), "USD");
        Money abs = sm.absolute();
        assertThat(abs.amount()).isEqualByComparingTo("12.3456");
        assertThat(abs.currency()).isEqualTo(Currency.getInstance("USD"));
    }

    @Test
    void equalsAndHashCode_considerValueAndCurrencyOnly() {
        SignedMoney sm1 = SignedMoney.of(BigDecimal.valueOf(10.0), "USD");
        SignedMoney sm2 = SignedMoney.of(BigDecimal.valueOf(10.0000), "USD");
        SignedMoney sm3 = SignedMoney.of(BigDecimal.valueOf(11), "USD");

        assertThat(sm1)
                .isEqualTo(sm2)
                .hasSameHashCodeAs((sm2))
                .isNotEqualTo(sm3);
    }

    @Test
    void of_withNullAmount_throwsException() {
        assertThatThrownBy(() -> SignedMoney.of(null, "USD"))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("amount cannot be null");
    }

    @Test
    void of_withNullCurrency_throwsException() {
        assertThatThrownBy(() -> SignedMoney.of(BigDecimal.ONE, (Currency) null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("currency cannot be null");
    }

    @Test
    void toString_containsAmountAndCurrency() {
        SignedMoney sm = SignedMoney.of(BigDecimal.valueOf(-12.3456), "USD");
        assertThat(sm.toString()).hasToString("-12.3456 USD");
    }
}
