package com.finledger.ledger_service.adapter.inbound.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finledger.ledger_service.application.port.inbound.OnSettlementCreatedUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class SettlementConsumerRabbitMQTest {

    private OnSettlementCreatedUseCase useCase;
    private ObjectMapper mapper;
    private SettlementConsumerRabbitMQ consumer;

    @BeforeEach
    void setUp() {
        useCase = mock(OnSettlementCreatedUseCase.class);
        mapper = new ObjectMapper()
                .registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule())
                .disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        consumer = new SettlementConsumerRabbitMQ(useCase, mapper);
    }

    @Test
    void onMessage_withValidJson_shouldCallUseCaseWithCorrectArguments() throws Exception {
        UUID eventId = UUID.randomUUID();
        Instant occurredAt = Instant.parse("2025-09-18T10:15:30Z");
        UUID settlementId = UUID.randomUUID();
        UUID buyerId = UUID.randomUUID();
        UUID sellerId = UUID.randomUUID();
        BigDecimal netAmount = BigDecimal.valueOf(123.45);
        String currency = "USD";

        SettlementConsumerRabbitMQ.SettlementCreatedEvent event =
                new SettlementConsumerRabbitMQ.SettlementCreatedEvent(
                        eventId, occurredAt, settlementId, buyerId, sellerId,
                        netAmount, currency, "SettlementCreated", "Settlement", settlementId
                );

        String json = mapper.writeValueAsString(event);

        consumer.onMessage(json);

        verify(useCase, times(1)).execute(eventId, settlementId, buyerId, sellerId, netAmount, currency);
        verifyNoMoreInteractions(useCase);
    }

    @Test
    void onMessage_withInvalidJson_shouldThrowIllegalArgumentException() {
        String invalidJson = "{ not-a-valid-json }";

        assertThatThrownBy(() -> consumer.onMessage(invalidJson))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid SettlementCreatedEvent payload");

        verifyNoInteractions(useCase);
    }
}
