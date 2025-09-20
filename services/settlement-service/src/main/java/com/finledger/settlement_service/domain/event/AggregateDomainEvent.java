package com.finledger.settlement_service.domain.event;

import java.util.UUID;

/**
 * A domain event that is tied to a specific aggregate. Provides metadata for outbox persistence and
 * filtering.
 */
public interface AggregateDomainEvent extends DomainEvent {

  /** The type of aggregate that produced this event (e.g., "Trade", "Settlement"). */
  String aggregateType();

  /** The unique identifier of the aggregate that produced this event. */
  UUID aggregateId();
}
