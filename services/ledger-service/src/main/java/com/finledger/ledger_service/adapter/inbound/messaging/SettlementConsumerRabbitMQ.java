package com.finledger.ledger_service.adapter.inbound.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.finledger.ledger_service.application.port.inbound.OnSettlementCreatedUseCase;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class SettlementConsumerRabbitMQ {

  private final OnSettlementCreatedUseCase useCase;
  private final ObjectMapper mapper;

  public record SettlementCreatedEvent(
      UUID eventId,
      Instant occurredAt,
      UUID settlementId,
      UUID buyerAccountId,
      UUID sellerAccountId,
      BigDecimal netAmount,
      String currency,
      String eventType,
      String aggregateType,
      UUID aggregateId) {}

  public SettlementConsumerRabbitMQ(OnSettlementCreatedUseCase useCase, ObjectMapper mapper) {
    this.useCase = useCase;
    this.mapper = mapper;
  }

  @RabbitListener(queues = "${messaging.settlement.queue}")
  public void onMessage(String json) {
    try {
      SettlementCreatedEvent event = mapper.readValue(json, SettlementCreatedEvent.class);
      useCase.execute(
          event.eventId(),
          event.settlementId(),
          event.buyerAccountId(),
          event.sellerAccountId(),
          event.netAmount(),
          event.currency());
    } catch (JsonProcessingException e) {
      throw new IllegalArgumentException("Invalid SettlementCreatedEvent payload", e);
    }
  }
}
