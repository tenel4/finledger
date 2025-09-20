package com.finledger.ledger_service.adapter.inbound.messaging;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finledger.ledger_service.application.port.inbound.OnSettlementCreatedUseCase;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers
class SettlementConsumerRabbitMQIT {

  @Container static RabbitMQContainer rabbit = new RabbitMQContainer("rabbitmq:3.12-management");

  @DynamicPropertySource
  static void rabbitProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.rabbitmq.host", rabbit::getHost);
    registry.add("spring.rabbitmq.port", rabbit::getAmqpPort);
    registry.add("spring.rabbitmq.username", rabbit::getAdminUsername);
    registry.add("spring.rabbitmq.password", rabbit::getAdminPassword);

    // override the queue name used in application.yml
    registry.add("messaging.settlement.queue", () -> "settlement.created.queue.test");
  }

  @Autowired private RabbitTemplate rabbitTemplate;

  @Autowired private ObjectMapper objectMapper;

  @MockitoSpyBean private OnSettlementCreatedUseCase useCase;

  @Test
  void whenValidMessagePublished_thenConsumerInvokesUseCase() throws Exception {
    // given
    SettlementConsumerRabbitMQ.SettlementCreatedEvent event =
        new SettlementConsumerRabbitMQ.SettlementCreatedEvent(
            UUID.randomUUID(),
            Instant.now(),
            UUID.randomUUID(),
            UUID.randomUUID(),
            UUID.randomUUID(),
            BigDecimal.valueOf(500),
            "USD",
            "SettlementCreated",
            "Settlement",
            UUID.randomUUID());

    String json = objectMapper.writeValueAsString(event);

    // when
    rabbitTemplate.convertAndSend("", "settlement.created.queue.test", json);

    // then
    ArgumentCaptor<UUID> eventIdCaptor = ArgumentCaptor.forClass(UUID.class);
    verify(useCase, timeout(5000))
        .execute(
            eventIdCaptor.capture(),
            ArgumentMatchers.eq(event.settlementId()),
            ArgumentMatchers.eq(event.buyerAccountId()),
            ArgumentMatchers.eq(event.sellerAccountId()),
            ArgumentMatchers.eq(event.netAmount()),
            ArgumentMatchers.eq(event.currency()));

    assertThat(eventIdCaptor.getValue()).isEqualTo(event.eventId());
  }

  @Test
  void whenInvalidJsonPublished_thenConsumerThrows() {
    // when
    rabbitTemplate.convertAndSend("", "settlement.created.queue.test", "{invalid-json}");

    // then
    // Spring AMQP will log/propagate the exception; here we just verify that
    // our mock useCase was never called
    verify(useCase, timeout(2000).times(0))
        .execute(
            org.mockito.ArgumentMatchers.any(),
            org.mockito.ArgumentMatchers.any(),
            org.mockito.ArgumentMatchers.any(),
            org.mockito.ArgumentMatchers.any(),
            org.mockito.ArgumentMatchers.any(),
            org.mockito.ArgumentMatchers.any());
  }
}
