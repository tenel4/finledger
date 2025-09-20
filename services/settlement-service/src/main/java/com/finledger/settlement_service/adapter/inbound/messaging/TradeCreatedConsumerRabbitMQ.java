package com.finledger.settlement_service.adapter.inbound.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.finledger.settlement_service.application.dto.TradeCreatedEventDto;
import com.finledger.settlement_service.application.port.inbound.OnTradeCreatedUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Component
public class TradeCreatedConsumerRabbitMQ {
  private static final Logger log = LoggerFactory.getLogger(TradeCreatedConsumerRabbitMQ.class);

  private final OnTradeCreatedUseCase useCase;
  private final ObjectMapper mapper;

  public TradeCreatedConsumerRabbitMQ(OnTradeCreatedUseCase useCase, ObjectMapper mapper) {
    this.useCase = useCase;
    this.mapper = mapper;
  }

  @RabbitListener(queues = "${messaging.trade.queue}")
  public void onMessage(
      String json, @Header(name = "x-correlation-id", required = false) String correlationId) {
    try {
      if (correlationId != null) {
        MDC.put("correlationId", correlationId);
      }
      String traceId = MDC.get("traceId");

      log.info(
          "Received TradeCreatedEventDto message correlationId={} traceId={}",
          correlationId,
          traceId);

      TradeCreatedEventDto dto = mapper.readValue(json, TradeCreatedEventDto.class);

      useCase.execute(dto);

      log.info(
          "Successfully processed TradeCreatedEventDto id={} correlationId={} traceId={}",
          dto.eventId(),
          correlationId,
          traceId);

    } catch (JsonProcessingException e) {
      log.error(
          "Failed to deserialize TradeCreatedEventDto, correlationId={}, payload={}",
          correlationId,
          json,
          e);
      throw new IllegalArgumentException("Invalid TradeCreatedEventDto payload", e);
    } catch (Exception e) {
      log.error("Error processing TradeCreatedEventDto, correlationId={}", correlationId, e);
      throw e;
    } finally {
      MDC.remove("correlationId");
    }
  }
}
