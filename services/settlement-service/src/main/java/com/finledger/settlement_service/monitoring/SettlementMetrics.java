package com.finledger.settlement_service.monitoring;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class SettlementMetrics {

  private final Counter settlementsCreated;

  public SettlementMetrics(MeterRegistry registry) {
    this.settlementsCreated =
        Counter.builder("settlements.created.total")
            .description("Number of settlements created")
            .tag("service", "settlement-service")
            .register(registry);
  }

  public void increment() {
    settlementsCreated.increment();
  }
}
