package com.finledger.settlement_service.domain.event;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public abstract class BaseDomainEvent implements DomainEvent {
    private final UUID eventId;
    private final Instant occurredAt;

    protected BaseDomainEvent() {
        this(UUID.randomUUID(), Instant.now());
    }

    protected BaseDomainEvent(UUID eventId, Instant occurredAt) {
        this.eventId = Objects.requireNonNull(eventId, "Event ID cannot be null");
        this.occurredAt = Objects.requireNonNull(occurredAt, "OccurredAt cannot be null");
    }

    @Override
    public UUID eventId() { return eventId; }
    @Override
    public Instant occurredAt() { return occurredAt; }
    @Override
    public String eventType() { return getClass().getSimpleName(); }
}
