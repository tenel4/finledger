package com.finledger.settlement_service.adapter.outbound.messaging;

import com.finledger.settlement_service.adapter.outbound.persistance.entity.OutboxEventEntity;
import com.finledger.settlement_service.adapter.outbound.persistance.repository.DeadOutboxEventJpaRepository;
import com.finledger.settlement_service.adapter.outbound.persistance.repository.OutboxEventJpaRepository;
import com.finledger.settlement_service.config.OutboxProperties;
import com.finledger.settlement_service.config.RabbitMQBindingsProperties;
import com.finledger.settlement_service.domain.model.OutboxStatus;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.lang.reflect.Method;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OutboxRabbitMQPublisherTest {

    @Mock OutboxEventJpaRepository outboxRepo;
    @Mock DeadOutboxEventJpaRepository deadRepo;
    @Mock RabbitTemplate rabbit;

    private OutboxProperties props;
    private RabbitMQBindingsProperties bindings;
    private OutboxRabbitMQPublisher publisher;

    @BeforeEach
    void setUp() {
        props = new OutboxProperties();
        props.setEnabled(true);
        props.setBatchSize(10);
        props.setMaxBatchesPerRun(1);
        props.setMaxRetries(2);
        props.setBackoffInitialMs(100);
        props.setBackoffMultiplier(2.0);
        props.setBackoffMaxMs(1000);

        bindings = new RabbitMQBindingsProperties();
        RabbitMQBindingsProperties.BindingProperties tradeBinding = new RabbitMQBindingsProperties.BindingProperties();
        tradeBinding.setExchange("ex.trade");
        tradeBinding.setRoutingKey("rk.trade");
        tradeBinding.setEventTypeHeader("TradeCreated");
        bindings.setTrade(tradeBinding);

        RabbitMQBindingsProperties.BindingProperties settlementBinding = new RabbitMQBindingsProperties.BindingProperties();
        settlementBinding.setExchange("ex.settlement");
        settlementBinding.setRoutingKey("rk.settlement");
        settlementBinding.setEventTypeHeader("SettlementCreated");
        bindings.setSettlement(settlementBinding);

         MeterRegistry metrics = new SimpleMeterRegistry();

        publisher = new OutboxRabbitMQPublisher(outboxRepo, deadRepo, rabbit, bindings, props, metrics);
    }

    private OutboxEventEntity newEvent(String type, int retryCount) {
        OutboxEventEntity e = new OutboxEventEntity();
        e.setId(UUID.randomUUID());
        e.setType(type);
        e.setPayload("{json}");
        e.setRetryCount(retryCount);
        e.setCreatedAt(Instant.now());
        return e;
    }

    @Test
    void processBatch_shouldSendAndMarkSent() {
        OutboxEventEntity event = newEvent("TradeCreated", 0);

        publisher.processBatch(List.of(event));

        verify(outboxRepo).updateStatus(eq(event.getId()), eq(OutboxStatus.PROCESSING), any());
        verify(rabbit).convertAndSend(
                eq("ex.trade"),
                eq("rk.trade"),
                eq("{json}"),
                any(org.springframework.amqp.core.MessagePostProcessor.class));
        verify(outboxRepo).updateDeliveryState(eq(event.getId()), eq(OutboxStatus.SENT),
                eq(0), isNull(), isNull(), any());
        verifyNoInteractions(deadRepo);
    }

    @Test
    void processBatch_shouldRetryOnFailure() {
        OutboxEventEntity event = newEvent("TradeCreated", 0);

        // Match the 4‑arg overload actually used in the code
        doThrow(new RuntimeException("broker down"))
                .when(rabbit).convertAndSend(
                        anyString(),
                        anyString(),
                        any(),
                        any(org.springframework.amqp.core.MessagePostProcessor.class)
                );

        publisher.processBatch(List.of(event));

        // Verify it was marked for retry
        verify(outboxRepo).updateDeliveryState(
                eq(event.getId()),
                eq(OutboxStatus.RETRY),
                eq(1),
                any(),                       // nextAttempt timestamp
                contains("broker down"),     // error message
                any()                        // updatedAt timestamp
        );

        // Should not have been moved to dead repo yet
        verifyNoInteractions(deadRepo);
    }

    @Test
    void processBatch_shouldMoveToDeadAfterMaxRetries() {
        OutboxEventEntity event = newEvent("TradeCreated", 2); // already at max

        doThrow(new RuntimeException("poison"))
                .when(rabbit).convertAndSend(
                        anyString(),
                        anyString(),
                        any(),
                        any(org.springframework.amqp.core.MessagePostProcessor.class),
                        any(org.springframework.amqp.rabbit.connection.CorrelationData.class)
                );

        publisher.processBatch(List.of(event));

        // intermediate status update
        verify(outboxRepo).updateStatus(eq(event.getId()), eq(OutboxStatus.PROCESSING), any());

        // dead repo save
        verify(deadRepo).save(any());

        // final delivery state update
        verify(outboxRepo).updateDeliveryState(
                eq(event.getId()),
                eq(OutboxStatus.DEAD),
                eq(2),
                isNull(),
                anyString(), // relax if message text is noisy
                any()
        );
    }

    @Test
    void processBatch_shouldSkipWhenNoBindingFound() {
        OutboxEventEntity event = newEvent("UnknownType", 0);

        publisher.processBatch(List.of(event));

        verifyNoInteractions(rabbit);
        verify(outboxRepo, never()).updateDeliveryState(any(), any(), anyInt(), any(), any(), any());
        verifyNoInteractions(deadRepo);
    }

    @Test
    void computeBackoff_shouldGrowExponentiallyAndCap() throws Exception {
        Method m = OutboxRabbitMQPublisher.class.getDeclaredMethod("computeBackoff", int.class);
        m.setAccessible(true);

        long first = (long) m.invoke(publisher, 1);
        long second = (long) m.invoke(publisher, 2);
        long capped = (long) m.invoke(publisher, 100);

        assertThat(first).isEqualTo(100);   // initial
        assertThat(second).isEqualTo(200);  // doubled
        assertThat(capped).isEqualTo(1000); // capped at max
    }

    @Test
    void truncate_shouldLimitLength() throws Exception {
        Method m = OutboxRabbitMQPublisher.class.getDeclaredMethod("truncate", String.class, int.class);
        m.setAccessible(true);

        String longStr = "x".repeat(2000);
        String truncated = (String) m.invoke(null, longStr, 1900);

        assertThat(truncated).hasSize(1900);
    }
}
