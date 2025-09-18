package com.finledger.settlement_service.adapter.outbound.messaging;

import com.finledger.settlement_service.adapter.outbound.persistance.entity.OutboxEventEntity;
import com.finledger.settlement_service.adapter.outbound.persistance.repository.DeadOutboxEventJpaRepository;
import com.finledger.settlement_service.adapter.outbound.persistance.repository.OutboxEventJpaRepository;
import com.finledger.settlement_service.config.OutboxProperties;
import com.finledger.settlement_service.config.RabbitMQBindingsProperties;
import com.finledger.settlement_service.domain.model.OutboxStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;


@Testcontainers
@SpringBootTest(
        properties = {
                "spring.rabbitmq.host=disabled",
                "spring.rabbitmq.listener.simple.auto-startup=false"
        }
)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class OutboxRabbitMQPublisherIT {

    @Autowired private OutboxEventJpaRepository outboxRepo;
    @Autowired private DeadOutboxEventJpaRepository deadRepo;
    @Autowired private OutboxRabbitMQPublisher publisher;

    @MockitoSpyBean private RabbitTemplate rabbit; // we mock RabbitMQ to simulate success/failure

    @Autowired private OutboxProperties props;
    @Autowired private RabbitMQBindingsProperties bindings;

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    private OutboxEventEntity newEvent(String type, OutboxStatus status, int retryCount) {
        OutboxEventEntity e = new OutboxEventEntity();
        e.setId(UUID.randomUUID());
        e.setType(type);
        e.setPayload("{json}");
        e.setStatus(status);
        e.setRetryCount(retryCount);
        e.setCreatedAt(Instant.now());
        return outboxRepo.save(e);
    }

    @BeforeEach
    void resetProps() {
        props.setEnabled(true);
        props.setBatchSize(10);
        props.setMaxBatchesPerRun(1);
        props.setMaxRetries(2);
    }

    @Test
    void scheduledFlush_shouldMarkEventAsSent() {
        OutboxEventEntity e = newEvent(bindings.getTrade().getEventTypeHeader(), OutboxStatus.PENDING, 0);
        outboxRepo.flush();

        // Stub send to succeed
        doNothing().when(rabbit).convertAndSend(
                eq(bindings.getTrade().getExchange()),
                eq(bindings.getTrade().getRoutingKey()),
                eq("{json}"),
                any(org.springframework.amqp.core.MessagePostProcessor.class)
        );

        publisher.scheduledFlush();

        OutboxEventEntity updated = outboxRepo.findById(e.getId()).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(OutboxStatus.SENT);

        verify(rabbit).convertAndSend(
                eq(bindings.getTrade().getExchange()),
                eq(bindings.getTrade().getRoutingKey()),
                eq("{json}"),
                any(org.springframework.amqp.core.MessagePostProcessor.class)
        );
    }

    @Test
    void scheduledFlush_shouldRetryOnFailure() {
        OutboxEventEntity e = newEvent(bindings.getTrade().getEventTypeHeader(), OutboxStatus.PENDING, 0);
        outboxRepo.flush(); // ensure committed

        doThrow(new RuntimeException("broker down"))
                .when(rabbit).convertAndSend(
                        eq(bindings.getTrade().getExchange()),
                        eq(bindings.getTrade().getRoutingKey()),
                        eq("{json}"),
                        any(org.springframework.amqp.core.MessagePostProcessor.class)
                );

        publisher.scheduledFlush();

        OutboxEventEntity updated = outboxRepo.findById(e.getId()).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(OutboxStatus.RETRY);
        assertThat(updated.getRetryCount()).isEqualTo(1);
    }

    @Test
    void scheduledFlush_shouldMoveToDeadAfterMaxRetries() {
        OutboxEventEntity e = newEvent(bindings.getTrade().getEventTypeHeader(), OutboxStatus.PENDING, props.getMaxRetries());
        doThrow(new RuntimeException("poison"))
                .when(rabbit).convertAndSend(anyString(), anyString(), any(), any(org.springframework.amqp.core.MessagePostProcessor.class));

        publisher.scheduledFlush();

        OutboxEventEntity updated = outboxRepo.findById(e.getId()).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(OutboxStatus.DEAD);

        assertThat(deadRepo.findAll()).hasSize(1);
    }
}


