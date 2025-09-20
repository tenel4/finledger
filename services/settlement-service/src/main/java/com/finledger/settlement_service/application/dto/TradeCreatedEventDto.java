package com.finledger.settlement_service.application.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.finledger.settlement_service.domain.event.AggregateDomainEvent;
import com.finledger.settlement_service.domain.event.TradeCreatedEvent;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.UUID;

/**
 * Serialization-safe representation of a TradeCreatedEvent for outbound messaging. Delegates
 * eventType, aggregateType, and aggregateId to the domain event.
 */
public record TradeCreatedEventDto(
    @JsonProperty("eventId") UUID eventId,
    @JsonProperty("occurredAt") @JsonFormat(shape = JsonFormat.Shape.STRING) Instant occurredAt,
    @JsonProperty("tradeId") UUID tradeId,
    @JsonProperty("grossAmount") String grossAmount, // fixed-scale string
    @JsonProperty("currency") String currency,
    @JsonProperty("tradeTime") @JsonFormat(shape = JsonFormat.Shape.STRING) Instant tradeTime,
    @JsonProperty("eventType") String eventType,
    @JsonProperty("aggregateType") String aggregateType,
    @JsonProperty("aggregateId") UUID aggregateId)
    implements AggregateDomainEvent {

  public static TradeCreatedEventDto fromDomain(TradeCreatedEvent event) {
    // Ensure BigDecimal is serialized with fixed scale (4 decimal places)
    String amountStr = event.grossAmount().setScale(4, RoundingMode.HALF_UP).toPlainString();

    return new TradeCreatedEventDto(
        event.eventId(),
        event.occurredAt(),
        event.tradeId(),
        amountStr,
        event.currency(),
        event.tradeTime(),
        event.eventType(), // pulled from domain event
        event.aggregateType(), // pulled from domain event
        event.aggregateId() // pulled from domain event
        );
  }
}
