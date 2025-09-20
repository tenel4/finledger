package com.finledger.ledger_service.domain.model;

import static org.assertj.core.api.Assertions.*;

import com.finledger.ledger_service.domain.value.SignedMoney;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class LedgerEntryTest {

  private static final SignedMoney CREDIT_10_USD =
      SignedMoney.of(BigDecimal.TEN, Currency.getInstance("USD"));
  private static final SignedMoney DEBIT_5_USD =
      SignedMoney.of(BigDecimal.valueOf(-5), Currency.getInstance("USD"));

  private static final SignedMoney AMOUNT =
      SignedMoney.of(BigDecimal.TEN, Currency.getInstance("USD"));
  private static final Instant NOW = Instant.now();
  private static final UUID ACCOUNT_ID = UUID.randomUUID();
  private static final LedgerEntryReferenceType REF_TYPE = LedgerEntryReferenceType.TRADE;
  private static final UUID REF_ID = UUID.randomUUID();
  private static final UUID MSG_ID = UUID.randomUUID();

  @Test
  void createNew_shouldPopulateFieldsAndGenerateIdAndTime() {
    UUID accountId = UUID.randomUUID();
    UUID referenceId = UUID.randomUUID();
    UUID messageId = UUID.randomUUID();

    LedgerEntry entry =
        LedgerEntry.createNew(
            accountId, CREDIT_10_USD, LedgerEntryReferenceType.TRADE, referenceId, messageId);

    assertThat(entry.id()).isNotNull();
    assertThat(entry.entryTime()).isNotNull();
    assertThat(entry.accountId()).isEqualTo(accountId);
    assertThat(entry.amount()).isEqualTo(CREDIT_10_USD);
    assertThat(entry.referenceType()).isEqualTo(LedgerEntryReferenceType.TRADE);
    assertThat(entry.referenceId()).isEqualTo(referenceId);
    assertThat(entry.messageId()).isEqualTo(messageId);
  }

  @Test
  void rehydrate_shouldPopulateAllFieldsExactly() {
    UUID id = UUID.randomUUID();
    Instant now = Instant.now();
    UUID accountId = UUID.randomUUID();
    UUID referenceId = UUID.randomUUID();
    UUID messageId = UUID.randomUUID();

    LedgerEntry entry =
        LedgerEntry.rehydrate(
            id,
            now,
            accountId,
            DEBIT_5_USD,
            LedgerEntryReferenceType.SETTLEMENT,
            referenceId,
            messageId);

    assertThat(entry.id()).isEqualTo(id);
    assertThat(entry.entryTime()).isEqualTo(now);
    assertThat(entry.accountId()).isEqualTo(accountId);
    assertThat(entry.amount()).isEqualTo(DEBIT_5_USD);
    assertThat(entry.referenceType()).isEqualTo(LedgerEntryReferenceType.SETTLEMENT);
    assertThat(entry.referenceId()).isEqualTo(referenceId);
    assertThat(entry.messageId()).isEqualTo(messageId);
  }

  @Test
  void isDebit_shouldDelegateToAmount() {
    LedgerEntry debitEntry =
        LedgerEntry.createNew(
            UUID.randomUUID(),
            DEBIT_5_USD,
            LedgerEntryReferenceType.TRADE,
            UUID.randomUUID(),
            UUID.randomUUID());
    assertThat(debitEntry.isDebit()).isTrue();
    assertThat(debitEntry.isCredit()).isFalse();
  }

  @Test
  void isCredit_shouldDelegateToAmount() {
    LedgerEntry creditEntry =
        LedgerEntry.createNew(
            UUID.randomUUID(),
            CREDIT_10_USD,
            LedgerEntryReferenceType.TRADE,
            UUID.randomUUID(),
            UUID.randomUUID());
    assertThat(creditEntry.isCredit()).isTrue();
    assertThat(creditEntry.isDebit()).isFalse();
  }

  @Test
  void equalsAndHashCode_shouldDependOnIdOnly() {
    UUID id = UUID.randomUUID();
    LedgerEntry e1 =
        LedgerEntry.rehydrate(
            id,
            Instant.now(),
            UUID.randomUUID(),
            CREDIT_10_USD,
            LedgerEntryReferenceType.TRADE,
            UUID.randomUUID(),
            UUID.randomUUID());
    LedgerEntry e2 =
        LedgerEntry.rehydrate(
            id,
            Instant.now(),
            UUID.randomUUID(),
            DEBIT_5_USD,
            LedgerEntryReferenceType.SETTLEMENT,
            UUID.randomUUID(),
            UUID.randomUUID());
    LedgerEntry e3 =
        LedgerEntry.rehydrate(
            UUID.randomUUID(),
            Instant.now(),
            UUID.randomUUID(),
            CREDIT_10_USD,
            LedgerEntryReferenceType.TRADE,
            UUID.randomUUID(),
            UUID.randomUUID());

    assertThat(e1).isEqualTo(e2).hasSameHashCodeAs(e2).isNotEqualTo(e3);
  }

  @Test
  void rehydrate_shouldThrowWhenIdIsNull() {
    assertThatThrownBy(
            () -> LedgerEntry.rehydrate(null, NOW, ACCOUNT_ID, AMOUNT, REF_TYPE, REF_ID, MSG_ID))
        .isInstanceOf(NullPointerException.class)
        .hasMessageContaining("ID cannot be null");
  }

  @Test
  void rehydrate_shouldThrowWhenEntryTimeIsNull() {
    UUID id = UUID.randomUUID();
    assertThatThrownBy(
            () -> LedgerEntry.rehydrate(id, null, ACCOUNT_ID, AMOUNT, REF_TYPE, REF_ID, MSG_ID))
        .isInstanceOf(NullPointerException.class)
        .hasMessageContaining("Entry Time cannot be null");
  }

  @Test
  void rehydrate_shouldThrowWhenAccountIdIsNull() {
    UUID id = UUID.randomUUID();
    assertThatThrownBy(() -> LedgerEntry.rehydrate(id, NOW, null, AMOUNT, REF_TYPE, REF_ID, MSG_ID))
        .isInstanceOf(NullPointerException.class)
        .hasMessageContaining("Account ID cannot be null");
  }

  @Test
  void rehydrate_shouldThrowWhenAmountIsNull() {
    UUID id = UUID.randomUUID();
    assertThatThrownBy(
            () -> LedgerEntry.rehydrate(id, NOW, ACCOUNT_ID, null, REF_TYPE, REF_ID, MSG_ID))
        .isInstanceOf(NullPointerException.class)
        .hasMessageContaining("Amount cannot be null");
  }

  @Test
  void rehydrate_shouldThrowWhenReferenceTypeIsNull() {
    UUID id = UUID.randomUUID();
    assertThatThrownBy(
            () -> LedgerEntry.rehydrate(id, NOW, ACCOUNT_ID, AMOUNT, null, REF_ID, MSG_ID))
        .isInstanceOf(NullPointerException.class)
        .hasMessageContaining("Reference Type cannot be null");
  }

  @Test
  void rehydrate_shouldThrowWhenReferenceIdIsNull() {
    UUID id = UUID.randomUUID();
    assertThatThrownBy(
            () -> LedgerEntry.rehydrate(id, NOW, ACCOUNT_ID, AMOUNT, REF_TYPE, null, MSG_ID))
        .isInstanceOf(NullPointerException.class)
        .hasMessageContaining("Reference ID cannot be null");
  }

  @Test
  void rehydrate_shouldThrowWhenMessageIdIsNull() {
    UUID id = UUID.randomUUID();
    assertThatThrownBy(
            () -> LedgerEntry.rehydrate(id, NOW, ACCOUNT_ID, AMOUNT, REF_TYPE, REF_ID, null))
        .isInstanceOf(NullPointerException.class)
        .hasMessageContaining("Message ID cannot be null");
  }
}
