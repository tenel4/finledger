package com.finledger.settlement_service.application.service;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.search.Search;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.springframework.stereotype.Service;

@Service
public class OutboxMetricsService {

  private final MeterRegistry metrics;

  public OutboxMetricsService(MeterRegistry metrics) {
    this.metrics = metrics;
  }

  public Map<String, Double> collectMetrics() {
    Map<String, Double> metricValues = new LinkedHashMap<>();

    // Gauges
    collectGauge(metricValues, "outbox.backlog.total");
    collectGauge(metricValues, "outbox.backlog.pending");
    collectGauge(metricValues, "outbox.backlog.retry");
    collectGauge(metricValues, "outbox.backlog.dead");

    // Counters
    collectCounter(metricValues, "outbox.sent.total");
    collectCounter(metricValues, "outbox.retry.total");
    collectCounter(metricValues, "outbox.dead.total");

    // Timers
    collectTimer(metricValues, "outbox.flush.duration", "outbox.flush.duration.meanMs");

    return metricValues;
  }

  private void collectGauge(Map<String, Double> target, String name) {
    Search.in(metrics).name(name).gauges().forEach(g -> target.put(name, g.value()));
  }

  private void collectCounter(Map<String, Double> target, String name) {
    Search.in(metrics).name(name).counters().forEach(c -> target.put(name, c.count()));
  }

  private void collectTimer(Map<String, Double> target, String name, String key) {
    Search.in(metrics)
        .name(name)
        .timers()
        .forEach(t -> target.put(key, t.mean(TimeUnit.MILLISECONDS)));
  }
}
