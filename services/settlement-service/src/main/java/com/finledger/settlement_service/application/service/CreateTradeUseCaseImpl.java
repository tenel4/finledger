package com.finledger.settlement_service.application.service;

import com.finledger.settlement_service.application.dto.TradeCreatedEventDto;
import com.finledger.settlement_service.application.port.inbound.CreateTradeUseCase;
import com.finledger.settlement_service.application.port.outbound.EventPublisher;
import com.finledger.settlement_service.application.port.outbound.TradeRepositoryPort;
import com.finledger.settlement_service.domain.event.TradeCreatedEvent;
import com.finledger.settlement_service.domain.exception.TradeCreationException;
import com.finledger.settlement_service.domain.model.Trade;
import com.finledger.settlement_service.domain.value.Money;
import com.finledger.settlement_service.domain.value.Quantity;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

@Service
public class CreateTradeUseCaseImpl implements CreateTradeUseCase {
  private static final Logger log = LoggerFactory.getLogger(CreateTradeUseCaseImpl.class);

  private final TradeRepositoryPort tradeRepositoryPort;
  private final EventPublisher eventPublisher;

  public CreateTradeUseCaseImpl(
      TradeRepositoryPort tradeRepositoryPort, EventPublisher eventPublisher) {
    this.tradeRepositoryPort = tradeRepositoryPort;
    this.eventPublisher = eventPublisher;
  }

  @Override
  @Transactional
  public Result execute(Command command) {
    String correlationId = MDC.get("correlationId");
    String traceId = MDC.get("traceId");

    log.info(
        "Starting trade creation: symbol={} side={} quantity={} price={} currency={} correlationId={} traceId={}",
        command.symbol(),
        command.side(),
        command.quantity(),
        command.price(),
        command.currency(),
        correlationId,
        traceId);

    log.debug("Full trade creation command: {}", command);

    Quantity quantity = Quantity.of(command.quantity());
    Money price = Money.of(command.price(), command.currency());
    Trade trade =
        Trade.createNew(
            command.symbol(),
            command.side(),
            quantity,
            price,
            command.buyerAccountId(),
            command.sellerAccountId());
    tradeRepositoryPort.save(trade);

    TradeCreatedEvent domainEvent = new TradeCreatedEvent(trade);
    TradeCreatedEventDto outboundDto = TradeCreatedEventDto.fromDomain(domainEvent);
    try {
      eventPublisher.publish(outboundDto);
    } catch (Exception e) {
      throw new TradeCreationException("Failed to create trade due to event persistence error", e);
    }

    log.info(
        "Trade created successfully: tradeId={} eventId={} correlationId={} traceId={}",
        trade.id(),
        domainEvent.eventId(),
        correlationId,
        traceId);

    return new Result(trade.id(), trade.tradeTime(), domainEvent.eventId());
  }
}
