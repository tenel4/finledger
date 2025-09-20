package com.finledger.settlement_service.domain;

import static org.assertj.core.api.Assertions.*;

import com.finledger.settlement_service.domain.model.Settlement;
import com.finledger.settlement_service.domain.value.Money;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class SettlementTest {

  private static final Currency USD = Currency.getInstance("USD");
  private final UUID tradeId = UUID.randomUUID();
  private final UUID messageId = UUID.randomUUID();
  private final LocalDate valueDate = LocalDate.now();
  private final Money grossAmount = Money.of(BigDecimal.valueOf(1000), USD);

  @Test
  void createNew_shouldCalculateFeesAndNetAmountCorrectly() {
    Settlement settlement =
        Settlement.createNew(tradeId, valueDate, grossAmount, Settlement.Status.PENDING, messageId);

    Money expectedFees = grossAmount.times(BigDecimal.valueOf(0.0003));
    Money expectedNet = grossAmount.subtract(expectedFees);

    assertThat(settlement.id()).isNotNull();
    assertThat(settlement.tradeId()).isEqualTo(tradeId);
    assertThat(settlement.valueDate()).isEqualTo(valueDate);
    assertThat(settlement.grossAmount()).isEqualTo(grossAmount);
    assertThat(settlement.fees()).isEqualTo(expectedFees);
    assertThat(settlement.netAmount()).isEqualTo(expectedNet);
    assertThat(settlement.status()).isEqualTo(Settlement.Status.PENDING);
    assertThat(settlement.messageId()).isEqualTo(messageId);
  }

  @Test
  void rehydrate_shouldSucceedWhenNetAmountMatchesGrossMinusFees() {
    Money fees = grossAmount.times(BigDecimal.valueOf(0.0003));
    Money net = grossAmount.subtract(fees);
    UUID id = UUID.randomUUID();

    Settlement settlement =
        Settlement.rehydrate(
            id, tradeId, valueDate, grossAmount, fees, net, Settlement.Status.SETTLED, messageId);

    assertThat(settlement.id()).isEqualTo(id);
    assertThat(settlement.status()).isEqualTo(Settlement.Status.SETTLED);
    assertThat(settlement.netAmount()).isEqualTo(net);
  }

  @Test
  void rehydrate_shouldThrowWhenNetAmountDoesNotMatch() {
    Money fees = Money.of(BigDecimal.TEN, USD);
    Money wrongNet = Money.of(BigDecimal.valueOf(999), USD);

    assertThatThrownBy(
            () ->
                Settlement.rehydrate(
                    UUID.randomUUID(),
                    tradeId,
                    valueDate,
                    grossAmount,
                    fees,
                    wrongNet,
                    Settlement.Status.FAILED,
                    messageId))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("netAmount must equal grossAmount - fees");
  }

  @Test
  void statusChecks_shouldReturnCorrectBooleans() {
    Settlement pending =
        Settlement.createNew(tradeId, valueDate, grossAmount, Settlement.Status.PENDING, messageId);
    Settlement settled =
        Settlement.createNew(tradeId, valueDate, grossAmount, Settlement.Status.SETTLED, messageId);
    Settlement failed =
        Settlement.createNew(tradeId, valueDate, grossAmount, Settlement.Status.FAILED, messageId);

    assertThat(pending.isPending()).isTrue();
    assertThat(pending.isSettled()).isFalse();
    assertThat(pending.isFailed()).isFalse();

    assertThat(settled.isSettled()).isTrue();
    assertThat(settled.isPending()).isFalse();
    assertThat(settled.isFailed()).isFalse();

    assertThat(failed.isFailed()).isTrue();
    assertThat(failed.isPending()).isFalse();
    assertThat(failed.isSettled()).isFalse();
  }

  @Test
  void equalsAndHashCode_shouldBeBasedOnId() {
    UUID id = UUID.randomUUID();
    Money fees = grossAmount.times(BigDecimal.valueOf(0.0003));
    Money net = grossAmount.subtract(fees);

    Settlement s1 =
        Settlement.rehydrate(
            id, tradeId, valueDate, grossAmount, fees, net, Settlement.Status.PENDING, messageId);
    Settlement s2 =
        Settlement.rehydrate(
            id, tradeId, valueDate, grossAmount, fees, net, Settlement.Status.PENDING, messageId);

    assertThat(s1).isEqualTo(s2).hasSameHashCodeAs(s2);
  }

  @Test
  void validate_shouldThrowWhenAnyArgumentIsNull() {
    UUID id = UUID.randomUUID();
    Money fees = grossAmount.times(BigDecimal.valueOf(0.0003));
    Money net = grossAmount.subtract(fees);

    assertThatThrownBy(
            () ->
                Settlement.rehydrate(
                    null,
                    tradeId,
                    valueDate,
                    grossAmount,
                    fees,
                    net,
                    Settlement.Status.PENDING,
                    messageId))
        .isInstanceOf(NullPointerException.class)
        .hasMessageContaining("ID cannot be null");

    assertThatThrownBy(
            () ->
                Settlement.rehydrate(
                    id,
                    null,
                    valueDate,
                    grossAmount,
                    fees,
                    net,
                    Settlement.Status.PENDING,
                    messageId))
        .isInstanceOf(NullPointerException.class)
        .hasMessageContaining("Trade ID cannot be null");

    assertThatThrownBy(
            () ->
                Settlement.rehydrate(
                    id,
                    tradeId,
                    null,
                    grossAmount,
                    fees,
                    net,
                    Settlement.Status.PENDING,
                    messageId))
        .isInstanceOf(NullPointerException.class)
        .hasMessageContaining("Value Date cannot be null");

    assertThatThrownBy(
            () ->
                Settlement.rehydrate(
                    id, tradeId, valueDate, null, fees, net, Settlement.Status.PENDING, messageId))
        .isInstanceOf(NullPointerException.class)
        .hasMessageContaining("Gross Amount cannot be null");

    assertThatThrownBy(
            () ->
                Settlement.rehydrate(
                    id,
                    tradeId,
                    valueDate,
                    grossAmount,
                    null,
                    net,
                    Settlement.Status.PENDING,
                    messageId))
        .isInstanceOf(NullPointerException.class)
        .hasMessageContaining("Fees cannot be null");

    assertThatThrownBy(
            () ->
                Settlement.rehydrate(
                    id,
                    tradeId,
                    valueDate,
                    grossAmount,
                    fees,
                    null,
                    Settlement.Status.PENDING,
                    messageId))
        .isInstanceOf(NullPointerException.class)
        .hasMessageContaining("Net Amount cannot be null");

    assertThatThrownBy(
            () ->
                Settlement.rehydrate(
                    id, tradeId, valueDate, grossAmount, fees, net, null, messageId))
        .isInstanceOf(NullPointerException.class)
        .hasMessageContaining("Status cannot be null");

    assertThatThrownBy(
            () ->
                Settlement.rehydrate(
                    id,
                    tradeId,
                    valueDate,
                    grossAmount,
                    fees,
                    net,
                    Settlement.Status.PENDING,
                    null))
        .isInstanceOf(NullPointerException.class)
        .hasMessageContaining("Message ID cannot be null");
  }
}
