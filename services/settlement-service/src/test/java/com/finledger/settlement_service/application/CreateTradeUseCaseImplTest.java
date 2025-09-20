package com.finledger.settlement_service.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import com.finledger.settlement_service.application.dto.TradeCreatedEventDto;
import com.finledger.settlement_service.application.port.inbound.CreateTradeUseCase;
import com.finledger.settlement_service.application.port.outbound.EventPublisher;
import com.finledger.settlement_service.application.port.outbound.TradeRepositoryPort;
import com.finledger.settlement_service.application.service.CreateTradeUseCaseImpl;
import com.finledger.settlement_service.domain.exception.TradeCreationException;
import com.finledger.settlement_service.domain.model.Trade;
import com.finledger.settlement_service.domain.value.Money;
import com.finledger.settlement_service.domain.value.Quantity;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class CreateTradeUseCaseImplTest {
  private TradeRepositoryPort tradeRepositoryPort;
  private EventPublisher eventPublisher;
  private CreateTradeUseCaseImpl useCase;

  @BeforeEach
  void setUp() {
    tradeRepositoryPort = mock(TradeRepositoryPort.class);
    eventPublisher = mock(EventPublisher.class);
    useCase = new CreateTradeUseCaseImpl(tradeRepositoryPort, eventPublisher);
  }

  private CreateTradeUseCase.Command sampleCommand() {
    return new CreateTradeUseCase.Command(
        "AAPL",
        Trade.Side.BUY,
        100L,
        BigDecimal.valueOf(150),
        "USD",
        UUID.randomUUID(),
        UUID.randomUUID());
  }

  @Test
  void execute_shouldSaveTradeAndPublishEvent_andReturnResult() {
    CreateTradeUseCase.Command command = sampleCommand();

    // Execute
    CreateTradeUseCase.Result result = useCase.execute(command);

    // Verify repository save was called with a Trade
    ArgumentCaptor<Trade> tradeCaptor = ArgumentCaptor.forClass(Trade.class);
    verify(tradeRepositoryPort).save(tradeCaptor.capture());
    Trade savedTrade = tradeCaptor.getValue();

    assertThat(savedTrade.symbol()).isEqualTo(command.symbol());
    assertThat(savedTrade.side()).isEqualTo(command.side());
    assertThat(savedTrade.quantity()).isEqualTo(Quantity.of(command.quantity()));
    assertThat(savedTrade.price()).isEqualTo(Money.of(command.price(), command.currency()));

    // Verify event publisher was called
    ArgumentCaptor<TradeCreatedEventDto> eventCaptor =
        ArgumentCaptor.forClass(TradeCreatedEventDto.class);
    verify(eventPublisher).publish(eventCaptor.capture());
    TradeCreatedEventDto publishedEvent = eventCaptor.getValue();

    assertThat(publishedEvent.tradeId()).isEqualTo(savedTrade.id());
    // Verify result matches trade and event
    assertThat(result.id()).isEqualTo(savedTrade.id());
    assertThat(result.tradeTime()).isBeforeOrEqualTo(Instant.now());
    assertThat(result.messageId()).isEqualTo(publishedEvent.eventId());
  }

  @Test
  void execute_shouldThrowTradeCreationException_whenEventPublisherFails() {
    CreateTradeUseCase.Command command = sampleCommand();

    doThrow(new RuntimeException("RabbitMQ down"))
        .when(eventPublisher)
        .publish(any(TradeCreatedEventDto.class));

    assertThatThrownBy(() -> useCase.execute(command))
        .isInstanceOf(TradeCreationException.class)
        .hasMessageContaining("Failed to create trade due to event persistence error")
        .hasCauseInstanceOf(RuntimeException.class);

    // Repository save should still have been called
    verify(tradeRepositoryPort).save(any(Trade.class));
  }
}
