package com.finledger.settlement_service.adapter.inbound.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finledger.settlement_service.application.dto.TradeCreatedEventDto;
import com.finledger.settlement_service.application.port.inbound.OnTradeCreatedUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class TradeCreatedConsumerRabbitMQTest {

    private OnTradeCreatedUseCase useCase;
    private ObjectMapper mapper;
    private TradeCreatedConsumerRabbitMQ consumer;

    @BeforeEach
    void setUp() {
        useCase = mock(OnTradeCreatedUseCase.class);
        mapper = new ObjectMapper()
                .registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule())
                .disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        consumer = new TradeCreatedConsumerRabbitMQ(useCase, mapper);
    }

    @Test
    void onMessage_shouldDeserializeAndCallUseCase() throws Exception {
        TradeCreatedEventDto dto = new TradeCreatedEventDto(
                UUID.randomUUID(),
                Instant.now(),
                UUID.randomUUID(),
                "1000.0000",
                "USD",
                Instant.now(),
                "TradeCreated",
                "Trade",
                UUID.randomUUID()
        );

        String json = mapper.writeValueAsString(dto);

        consumer.onMessage(json, "corr-123");

        ArgumentCaptor<TradeCreatedEventDto> captor = ArgumentCaptor.forClass(TradeCreatedEventDto.class);
        verify(useCase).execute(captor.capture());

        assertThat(captor.getValue().tradeId()).isEqualTo(dto.tradeId());
    }

    @Test
    void onMessage_shouldThrowIllegalArgumentException_whenJsonInvalid() {
        String invalidJson = "{ not-valid-json }";

        assertThatThrownBy(() -> consumer.onMessage(invalidJson, "corr-123"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid TradeCreatedEventDto payload");

        verifyNoInteractions(useCase);
    }

    @Test
    void onMessage_shouldPropagateExceptionFromUseCase() throws Exception {
        TradeCreatedEventDto dto = new TradeCreatedEventDto(
                UUID.randomUUID(),
                Instant.now(),
                UUID.randomUUID(),
                "1000.0000",
                "USD",
                Instant.now(),
                "TradeCreated",
                "Trade",
                UUID.randomUUID()
        );

        String json = mapper.writeValueAsString(dto);

        doThrow(new RuntimeException("DB down")).when(useCase).execute(any());

        assertThatThrownBy(() -> consumer.onMessage(json, "corr-123"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("DB down");
    }
}
