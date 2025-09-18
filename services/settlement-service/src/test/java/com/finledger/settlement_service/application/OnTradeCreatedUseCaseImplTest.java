package com.finledger.settlement_service.application;

import com.finledger.settlement_service.application.dto.TradeCreatedEventDto;
import com.finledger.settlement_service.application.port.outbound.*;
import com.finledger.settlement_service.application.service.OnTradeCreatedUseCaseImpl;
import com.finledger.settlement_service.domain.exception.SettlementCreationException;
import com.finledger.settlement_service.domain.model.*;
import com.finledger.settlement_service.domain.service.ValueDateCalculator;
import com.finledger.settlement_service.domain.value.Money;
import com.finledger.settlement_service.domain.value.Quantity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Currency;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class OnTradeCreatedUseCaseImplTest {

    private TradeRepositoryPort tradeRepositoryPort;
    private SettlementRepositoryPort settlementRepositoryPort;
    private ValueDateCalculator valueDateCalculator;
    private EventPublisher eventPublisher;

    private OnTradeCreatedUseCaseImpl useCase;

    private final UUID tradeId = UUID.randomUUID();
    private final UUID buyerId = UUID.randomUUID();
    private final UUID sellerId = UUID.randomUUID();
    private final UUID eventId = UUID.randomUUID();
    private final Instant tradeTime = Instant.parse("2025-01-01T10:15:30Z");
    private final Instant occurredAt = Instant.parse("2025-01-01T10:15:31Z");

    @BeforeEach
    void setUp() {
        tradeRepositoryPort = mock(TradeRepositoryPort.class);
        settlementRepositoryPort = mock(SettlementRepositoryPort.class);
        valueDateCalculator = mock(ValueDateCalculator.class);
        eventPublisher = mock(EventPublisher.class);

        useCase = new OnTradeCreatedUseCaseImpl(
                tradeRepositoryPort,
                settlementRepositoryPort,
                valueDateCalculator,
                eventPublisher
        );
    }

    private TradeCreatedEventDto sampleEvent() {
        return new TradeCreatedEventDto(
                eventId,
                occurredAt,
                tradeId,
                "1000.0000",               // grossAmount as String
                "USD",
                tradeTime,
                "trade.created",            // eventType
                "Trade",                   // aggregateType
                tradeId                    // aggregateId
        );
    }

    private Trade sampleTrade() {
        return Trade.createNew(
                "AAPL",
                Trade.Side.BUY,
                Quantity.of(10L),
                Money.of(BigDecimal.valueOf(100), Currency.getInstance("USD")),
                buyerId,
                sellerId
        );
    }

    @Test
    void execute_shouldCreateSettlementAndPublishEvent() {
        TradeCreatedEventDto event = sampleEvent();
        Trade trade = sampleTrade();
        LocalDate valueDate = LocalDate.of(2025, 1, 3);

        when(tradeRepositoryPort.findById(tradeId)).thenReturn(Optional.of(trade));
        when(valueDateCalculator.calculate(tradeTime)).thenReturn(valueDate);

        useCase.execute(event);

        // Verify settlement saved
        ArgumentCaptor<Settlement> settlementCaptor = ArgumentCaptor.forClass(Settlement.class);
        verify(settlementRepositoryPort).save(settlementCaptor.capture());
        Settlement savedSettlement = settlementCaptor.getValue();

        assertThat(savedSettlement.tradeId()).isEqualTo(tradeId);
        assertThat(savedSettlement.valueDate()).isEqualTo(valueDate);
        assertThat(savedSettlement.status()).isEqualTo(Settlement.Status.PENDING);

        // Verify event published
        verify(eventPublisher).publish(any());
    }

    @Test
    void execute_shouldThrowWhenTradeNotFound() {
        TradeCreatedEventDto event = sampleEvent();
        when(tradeRepositoryPort.findById(tradeId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(event))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Trade not found for id: " + tradeId);

        verifyNoInteractions(settlementRepositoryPort, valueDateCalculator, eventPublisher);
    }

    @Test
    void execute_shouldThrowSettlementCreationExceptionWhenPublisherFails() {
        TradeCreatedEventDto event = sampleEvent();
        Trade trade = sampleTrade();
        LocalDate valueDate = LocalDate.of(2025, 1, 3);

        when(tradeRepositoryPort.findById(tradeId)).thenReturn(Optional.of(trade));
        when(valueDateCalculator.calculate(tradeTime)).thenReturn(valueDate);
        doThrow(new RuntimeException("Kafka down")).when(eventPublisher).publish(any());

        assertThatThrownBy(() -> useCase.execute(event))
                .isInstanceOf(SettlementCreationException.class)
                .hasMessageContaining("Failed to create settlement due to event persistence error")
                .hasCauseInstanceOf(RuntimeException.class);

        verify(settlementRepositoryPort).save(any(Settlement.class));
        verify(eventPublisher).publish(any());
    }
}
