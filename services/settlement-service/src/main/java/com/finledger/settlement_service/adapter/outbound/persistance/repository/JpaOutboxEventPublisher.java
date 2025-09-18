package com.finledger.settlement_service.adapter.outbound.persistance.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.finledger.settlement_service.adapter.outbound.persistance.entity.OutboxEventEntity;
import com.finledger.settlement_service.application.port.outbound.EventPublisher;
import com.finledger.settlement_service.domain.event.AggregateDomainEvent;
import com.finledger.settlement_service.domain.event.DomainEvent;
import com.finledger.settlement_service.domain.exception.EventPersistenceException;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

@Component
public class JpaOutboxEventPublisher implements EventPublisher {
    private static final Logger log = LoggerFactory.getLogger(JpaOutboxEventPublisher.class);

    private final OutboxEventJpaRepository repo;
    private final ObjectMapper mapper;

    public JpaOutboxEventPublisher(OutboxEventJpaRepository repo, ObjectMapper mapper) {
        this.repo = repo;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public void publish(DomainEvent event) {
        String correlationId = MDC.get("correlationId");
        String traceId = MDC.get("traceId");

        try {
            String payload = mapper.writeValueAsString(event);

            String aggregateType = null;
            String aggregateId = null;

            if (event instanceof AggregateDomainEvent agg) {
                aggregateType = agg.aggregateType();
                aggregateId = agg.aggregateId().toString();
            }

            OutboxEventEntity entity = OutboxEventEntity.pending(
                    event.eventId(),
                    event.eventType(),
                    aggregateType,
                    aggregateId,
                    payload
            );

            repo.save(entity);

            if (log.isInfoEnabled()) {
                log.info("Outbox event persisted successfully: eventId={} eventType={} aggregateType={} aggregateId={} correlationId={} traceId={}",
                        event.eventId(), event.eventType(), aggregateType, aggregateId, correlationId, traceId);
            }
            if (log.isDebugEnabled()) {
                log.debug("Outbox event payload: {}", payload);
            }

        } catch (JsonProcessingException e) {
            log.error("Failed to serialize domain event: eventType={} eventId={} correlationId={} traceId={}",
                    event.eventType(), event.eventId(), correlationId, traceId, e);
            throw new EventPersistenceException("Failed to serialize domain event: " + event.eventType(), e);
        } catch (RuntimeException e) {
            log.error("Failed to persist domain event: eventType={} eventId={} correlationId={} traceId={}",
                    event.eventType(), event.eventId(), correlationId, traceId, e);
            throw new EventPersistenceException("Failed to persist domain event: " + event.eventType(), e);
        }
    }
}
