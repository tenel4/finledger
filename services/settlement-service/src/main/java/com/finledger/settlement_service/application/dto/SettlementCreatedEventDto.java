package com.finledger.settlement_service.application.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.finledger.settlement_service.domain.event.AggregateDomainEvent;
import com.finledger.settlement_service.domain.event.SettlementCreatedEvent;

import java.math.RoundingMode;
import java.time.Instant;
import java.util.UUID;

/**
 * Serialization-safe representation of a SettlementCreatedEvent for outbound messaging.
 * Delegates eventType, aggregateType, and aggregateId to the domain event.
 */
public record SettlementCreatedEventDto(
        @JsonProperty("eventId") UUID eventId,
        @JsonProperty("occurredAt") @JsonFormat(shape = JsonFormat.Shape.STRING) Instant occurredAt,
        @JsonProperty("settlementId") UUID settlementId,
        @JsonProperty("buyerAccountId") UUID buyerAccountId,
        @JsonProperty("sellerAccountId") UUID sellerAccountId,
        @JsonProperty("netAmount") String netAmount, // fixed-scale string
        @JsonProperty("currency") String currency,
        @JsonProperty("eventType") String eventType,
        @JsonProperty("aggregateType") String aggregateType,
        @JsonProperty("aggregateId") UUID aggregateId
) implements AggregateDomainEvent {

    public static SettlementCreatedEventDto fromDomain(SettlementCreatedEvent event) {
        // Ensure BigDecimal is serialized with fixed scale (4 decimal places)
        String amountStr = event.netAmount()
                .setScale(4, RoundingMode.HALF_UP)
                .toPlainString();

        return new SettlementCreatedEventDto(
                event.eventId(),
                event.occurredAt(),
                event.settlementId(),
                event.buyerAccountId(),
                event.sellerAccountId(),
                amountStr,
                event.currency(),
                event.eventType(),       // pulled from domain event
                event.aggregateType(),   // pulled from domain event
                event.aggregateId()      // pulled from domain event
        );
    }
}
