package com.finledger.settlement_service.adapter.outbound.messaging;

import com.finledger.settlement_service.adapter.outbound.persistance.entity.DeadOutboxEventEntity;
import com.finledger.settlement_service.adapter.outbound.persistance.entity.OutboxEventEntity;
import com.finledger.settlement_service.adapter.outbound.persistance.repository.DeadOutboxEventJpaRepository;
import com.finledger.settlement_service.adapter.outbound.persistance.repository.OutboxEventJpaRepository;
import com.finledger.settlement_service.config.OutboxProperties;
import com.finledger.settlement_service.config.RabbitMQBindingsProperties;
import com.finledger.settlement_service.domain.model.OutboxStatus;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
public class OutboxRabbitMQPublisher {

    private static final String METRICS_TAG_EXCHANGE = "exchange";

    private static final Logger log = LoggerFactory.getLogger(OutboxRabbitMQPublisher.class);

    private final OutboxEventJpaRepository outboxRepo;
    private final DeadOutboxEventJpaRepository deadRepo;
    private final RabbitTemplate rabbit;
    private final Map<String, RabbitMQBindingsProperties.BindingProperties> bindingsByEventType;
    private final OutboxProperties props;
    private final MeterRegistry metrics;

    public OutboxRabbitMQPublisher(OutboxEventJpaRepository outboxRepo,
                                   DeadOutboxEventJpaRepository deadRepo,
                                   RabbitTemplate rabbit,
                                   RabbitMQBindingsProperties bindingProps,
                                   OutboxProperties props,
                                   MeterRegistry metrics) {
        this.outboxRepo = outboxRepo;
        this.deadRepo = deadRepo;
        this.rabbit = rabbit;
        this.bindingsByEventType = Map.of(
                bindingProps.getTrade().getEventTypeHeader(), bindingProps.getTrade(),
                bindingProps.getSettlement().getEventTypeHeader(), bindingProps.getSettlement()
        );
        this.props = props;
        this.metrics = metrics;

        // Register gauges with enum-safe calls
        metrics.gauge("outbox.backlog.total", this,
                p -> p.outboxRepo.countByStatuses(List.of(OutboxStatus.PENDING, OutboxStatus.RETRY)));
        metrics.gauge("outbox.backlog.pending", this,
                p -> p.outboxRepo.countByStatus(OutboxStatus.PENDING));
        metrics.gauge("outbox.backlog.retry", this,
                p -> p.outboxRepo.countByStatus(OutboxStatus.RETRY));
        metrics.gauge("outbox.backlog.dead", this,
                p -> p.outboxRepo.countByStatus(OutboxStatus.DEAD));
    }

    @Scheduled(fixedDelayString = "${outbox.flush.fixedDelayMs:30000}")
    public void scheduledFlush() {
        if (!props.isEnabled()) {
            if (log.isDebugEnabled()) {
                log.debug("Outbox flush skipped because it is disabled");
            }
            return;
        }

        String correlationId = MDC.get("correlationId");
        String traceId = MDC.get("traceId");

        int batches = 0;
        int totalProcessed = 0;
        int totalRetries = 0;
        int totalDead = 0;
        long start = System.currentTimeMillis();

        if (log.isInfoEnabled()) {
            log.info("Starting outbox flush: maxBatches={} batchSize={} correlationId={} traceId={}",
                    props.getMaxBatchesPerRun(), props.getBatchSize(), correlationId, traceId);
        }

        while (batches < props.getMaxBatchesPerRun()) {
            List<OutboxEventEntity> batch = lockDueBatch();
            if (batch.isEmpty()) break;

            BatchResult result = processBatch(batch);
            totalProcessed += result.successCount;
            totalRetries += result.retryCount;
            totalDead += result.deadCount;
            batches++;
        }

        metrics.counter("outbox.sent.total").increment(totalProcessed);
        metrics.counter("outbox.retry.total").increment(totalRetries);
        metrics.counter("outbox.dead.total").increment(totalDead);
        metrics.timer("outbox.flush.duration")
                .record(System.currentTimeMillis() - start, TimeUnit.MILLISECONDS);

        if (log.isInfoEnabled()) {
            log.info("Completed outbox flush: batches={} sent={} retries={} dead={} durationMs={} correlationId={} traceId={}",
                    batches, totalProcessed, totalRetries, totalDead,
                    (System.currentTimeMillis() - start), correlationId, traceId);
        }
    }

    @Transactional
    protected List<OutboxEventEntity> lockDueBatch() {
        return outboxRepo.lockDueBatch(props.getBatchSize());
    }

    @Transactional
    protected BatchResult processBatch(List<OutboxEventEntity> batch) {
        int success = 0;
        int retries = 0;
        int dead = 0;
        String correlationId = MDC.get("correlationId");
        String traceId = MDC.get("traceId");

        for (OutboxEventEntity e : batch) {
            RabbitMQBindingsProperties.BindingProperties binding = bindingsByEventType.get(e.getType());
            if (binding == null) {
                log.error("No binding found for event type={} correlationId={} traceId={}",
                        e.getType(), correlationId, traceId);
                continue;
            }
            try {
                outboxRepo.updateStatus(e.getId(), OutboxStatus.PROCESSING, Instant.now());

                rabbit.convertAndSend(binding.getExchange(), binding.getRoutingKey(), e.getPayload(),
                        message -> {
                            message.getMessageProperties().setMessageId(e.getId().toString());
                            message.getMessageProperties().setContentType("application/json");
                            message.getMessageProperties().setHeader("eventType", e.getType());
                            return message;
                        });

                outboxRepo.updateDeliveryState(e.getId(), OutboxStatus.SENT, e.getRetryCount(), null, null, Instant.now());
                metrics.counter("outbox.sent", METRICS_TAG_EXCHANGE, binding.getExchange(), "rk", binding.getRoutingKey()).increment();
                success++;

                if (log.isDebugEnabled()) {
                    log.debug("Outbox event sent: id={} type={} exchange={} rk={} correlationId={} traceId={}",
                            e.getId(), e.getType(), binding.getExchange(), binding.getRoutingKey(), correlationId, traceId);
                }

            } catch (Exception ex) {
                String err = truncate(ex.getMessage(), 1900);
                int nextRetry = e.getRetryCount() + 1;

                if (nextRetry > props.getMaxRetries()) {
                    DeadOutboxEventEntity deadEntity = new DeadOutboxEventEntity();
                    deadEntity.setId(e.getId());
                    deadEntity.setType(e.getType());
                    deadEntity.setPayload(e.getPayload());
                    deadEntity.setRetryCount(nextRetry - 1);
                    deadEntity.setLastError(err);
                    deadEntity.setCreatedAt(e.getCreatedAt());
                    deadEntity.setDeadAt(Instant.now());
                    deadRepo.save(deadEntity);

                    outboxRepo.updateDeliveryState(e.getId(), OutboxStatus.DEAD, nextRetry - 1, null, err, Instant.now());
                    metrics.counter("outbox.dead", METRICS_TAG_EXCHANGE, binding.getExchange(), "rk", binding.getRoutingKey()).increment();
                    log.error("Poison message moved to dead_outbox_event id={} type={} exchange={} rk={} error={} correlationId={} traceId={}",
                            e.getId(), e.getType(), binding.getExchange(), binding.getRoutingKey(), err, correlationId, traceId);
                    dead++;

                } else {
                    long backoff = computeBackoff(nextRetry);
                    Instant nextAttempt = Instant.now().plusMillis(backoff);
                    outboxRepo.updateDeliveryState(e.getId(), OutboxStatus.RETRY, nextRetry, nextAttempt, err, Instant.now());
                    metrics.counter("outbox.retry", METRICS_TAG_EXCHANGE, binding.getExchange(), "rk", binding.getRoutingKey()).increment();
                    log.warn("Outbox publish failed; scheduling retry={} in {} ms id={} type={} exchange={} rk={} error={} correlationId={} traceId={}",
                            nextRetry, backoff, e.getId(), e.getType(), binding.getExchange(), binding.getRoutingKey(), err, correlationId, traceId);
                    retries++;
                }
            }
        }
        return new BatchResult(success, retries, dead);
    }

    private long computeBackoff(int attempt) {
        double delay = props.getBackoffInitialMs() * Math.pow(props.getBackoffMultiplier(), Math.max(0, attempt - 1));
        return Math.min((long) delay, props.getBackoffMaxMs());
    }

    private static String truncate(String s, int max) {
        if (s == null) return null;
        return s.length() <= max ? s : s.substring(0, max);
    }

    private static class BatchResult {
        final int successCount;
        final int retryCount;
        final int deadCount;

        BatchResult(int successCount, int retryCount, int deadCount) {
            this.successCount = successCount;
            this.retryCount = retryCount;
            this.deadCount = deadCount;
        }
    }
}
