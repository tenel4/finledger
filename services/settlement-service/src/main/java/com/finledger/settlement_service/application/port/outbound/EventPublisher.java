package com.finledger.settlement_service.application.port.outbound;

import com.finledger.settlement_service.domain.event.DomainEvent;

public interface EventPublisher {
    void publish(DomainEvent event);
}
