package com.finledger.settlement_service.adapter.inbound.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finledger.settlement_service.application.dto.TradeCreatedEventDto;
import com.finledger.settlement_service.application.port.inbound.OnTradeCreatedUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.util.UUID;

import static org.awaitility.Awaitility.await;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@SpringBootTest
@Testcontainers
class TradeCreatedConsumerRabbitMQIT {

    @Container
    static RabbitMQContainer rabbit = new RabbitMQContainer("rabbitmq:3.12-management")
            .withExposedPorts(5672, 15672);

    @DynamicPropertySource
    static void rabbitProps(DynamicPropertyRegistry registry) {
        registry.add("spring.rabbitmq.host", rabbit::getHost);
        registry.add("spring.rabbitmq.port", rabbit::getAmqpPort);
        registry.add("spring.rabbitmq.username", rabbit::getAdminUsername);
        registry.add("spring.rabbitmq.password", rabbit::getAdminPassword);
        // Ensure the queue name matches your @RabbitListener
        registry.add("messaging.trade.queue", () -> "trade.queue");
    }

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private OnTradeCreatedUseCase useCase;

    @Test
    void shouldConsumeMessageFromRabbitMQ() throws Exception {
        // Arrange: build a TradeCreatedEvent matching your record
        TradeCreatedEventDto event = new TradeCreatedEventDto(
                UUID.randomUUID(),
                Instant.now(),
                UUID.randomUUID(),
                "1000.0000",
                "USD",
                Instant.now(),
                "trade.created",
                "Trade",
                UUID.randomUUID()
        );

        String json = objectMapper.writeValueAsString(event);

        // Act: send the message to the queue
        rabbitTemplate.convertAndSend("trade.exchange", "trade.created", json,
                m -> { m.getMessageProperties().setHeader("x-correlation-id", "corr-123"); return m; });
        // Assert: wait until the listener processes it
        await().atMost(5, SECONDS).untilAsserted(() ->
                verify(useCase).execute(any(TradeCreatedEventDto.class))
        );
    }
}
