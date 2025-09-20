package com.finledger.settlement_service.adapter.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.*;
import org.junit.jupiter.api.Test;

class SimpleValueDateCalculatorTest {

  private final SimpleValueDateCalculator calculator = new SimpleValueDateCalculator();

  @Test
  void calculate_shouldAddTwoDaysToLondonLocalDate() {
    // Arrange: 2025-09-10T10:15:30Z is 2025-09-10 11:15:30 in London (BST)
    Instant tradeTime = Instant.parse("2025-09-10T10:15:30Z");

    // Act
    LocalDate valueDate = calculator.calculate(tradeTime);

    // Assert: London local date is 2025-09-10, plus 2 days = 2025-09-12
    assertThat(valueDate).isEqualTo(LocalDate.of(2025, 9, 12));
  }

  @Test
  void calculate_shouldHandleDaylightSavingTimeEnd() {
    // Arrange: DST in London ends on 2025-10-26 at 02:00 local time
    // This instant is 2025-10-25T23:30:00Z, which is 2025-10-26 00:30 BST
    Instant tradeTime = Instant.parse("2025-10-25T23:30:00Z");

    // Act
    LocalDate valueDate = calculator.calculate(tradeTime);

    // Assert: London local date is 2025-10-26, plus 2 days = 2025-10-28
    assertThat(valueDate).isEqualTo(LocalDate.of(2025, 10, 28));
  }
}
