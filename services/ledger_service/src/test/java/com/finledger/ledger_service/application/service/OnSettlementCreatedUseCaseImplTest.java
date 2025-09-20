package com.finledger.ledger_service.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.finledger.ledger_service.application.port.outbound.LedgerRepositoryPort;
import com.finledger.ledger_service.application.port.outbound.ProcessedMessageRepositoryPort;
import com.finledger.ledger_service.domain.model.LedgerEntry;
import com.finledger.ledger_service.domain.model.LedgerEntryReferenceType;
import com.finledger.ledger_service.domain.value.SignedMoney;
import java.math.BigDecimal;
import java.util.Currency;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class OnSettlementCreatedUseCaseImplTest {

  private LedgerRepositoryPort ledgerRepo;
  private ProcessedMessageRepositoryPort processedMessageRepo;
  private OnSettlementCreatedUseCaseImpl useCase;

  @BeforeEach
  void setUp() {
    ledgerRepo = mock(LedgerRepositoryPort.class);
    processedMessageRepo = mock(ProcessedMessageRepositoryPort.class);
    useCase = new OnSettlementCreatedUseCaseImpl(ledgerRepo, processedMessageRepo);
  }

  @Test
  void execute_whenMessageIsNew_shouldCreateAndSaveTwoEntries() {
    UUID messageKey = UUID.randomUUID();
    UUID settlementId = UUID.randomUUID();
    UUID buyerId = UUID.randomUUID();
    UUID sellerId = UUID.randomUUID();
    BigDecimal netAmount = BigDecimal.valueOf(100.25);
    String currency = "USD";

    when(processedMessageRepo.markProcessedIfNew(messageKey)).thenReturn(true);

    useCase.execute(messageKey, settlementId, buyerId, sellerId, netAmount, currency);

    // Capture and verify the saved entries
    verify(ledgerRepo, times(1))
        .saveAll(
            argThat(
                entries -> {
                  assertThat(entries).hasSize(2);

                  LedgerEntry buyerEntry = entries.getFirst();
                  assertThat(buyerEntry.accountId()).isEqualTo(buyerId);
                  assertThat(buyerEntry.amount())
                      .isEqualTo(
                          SignedMoney.of(netAmount.negate(), Currency.getInstance(currency)));
                  assertThat(buyerEntry.referenceType())
                      .isEqualTo(LedgerEntryReferenceType.SETTLEMENT);
                  assertThat(buyerEntry.referenceId()).isEqualTo(settlementId);
                  assertThat(buyerEntry.messageId()).isEqualTo(messageKey);

                  LedgerEntry sellerEntry = entries.get(1);
                  assertThat(sellerEntry.accountId()).isEqualTo(sellerId);
                  assertThat(sellerEntry.amount())
                      .isEqualTo(SignedMoney.of(netAmount, Currency.getInstance(currency)));
                  assertThat(sellerEntry.referenceType())
                      .isEqualTo(LedgerEntryReferenceType.SETTLEMENT);
                  assertThat(sellerEntry.referenceId()).isEqualTo(settlementId);
                  assertThat(sellerEntry.messageId()).isEqualTo(messageKey);

                  return true;
                }));

    verify(processedMessageRepo, times(1)).markProcessedIfNew(messageKey);
    verifyNoMoreInteractions(ledgerRepo, processedMessageRepo);
  }

  @Test
  void execute_whenMessageAlreadyProcessed_shouldDoNothing() {
    UUID messageKey = UUID.randomUUID();

    when(processedMessageRepo.markProcessedIfNew(messageKey)).thenReturn(false);

    useCase.execute(
        messageKey, UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), BigDecimal.TEN, "USD");

    verify(processedMessageRepo, times(1)).markProcessedIfNew(messageKey);
    verifyNoInteractions(ledgerRepo);
  }
}
