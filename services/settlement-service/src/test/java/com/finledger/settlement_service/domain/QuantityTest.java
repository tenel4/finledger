package com.finledger.settlement_service.domain;

import com.finledger.settlement_service.domain.value.Quantity;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class QuantityTest {

    @Test
    void of_shouldCreateQuantity_whenValueIsPositive() {
        long value = 10;
        Quantity quantity = Quantity.of(value);
        assertThat(quantity.value()).isEqualTo(10);
    }

    @Test
    void of_shouldThrowException_whenValueIsZero() {
        assertThatThrownBy(() -> Quantity.of(0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Quantity must be greater than zero, but was: 0");
    }

    @Test
    void of_shouldThrowException_whenValueIsNegative() {
        assertThatThrownBy(() -> Quantity.of(-5))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Quantity must be greater than zero, but was: -5");
    }
}
