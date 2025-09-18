package com.finledger.settlement_service.adapter.inbound.rest;

import com.finledger.settlement_service.adapter.outbound.persistance.entity.DeadOutboxEventEntity;
import com.finledger.settlement_service.adapter.outbound.persistance.entity.OutboxEventEntity;
import com.finledger.settlement_service.adapter.outbound.persistance.repository.DeadOutboxEventJpaRepository;
import com.finledger.settlement_service.adapter.outbound.persistance.repository.OutboxEventJpaRepository;
import com.finledger.settlement_service.application.service.OutboxMetricsService;
import com.finledger.settlement_service.domain.model.OutboxStatus;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.data.domain.*;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/admin/outbox")
public class OutboxAdminController {

    private final OutboxEventJpaRepository outboxRepo;
    private final DeadOutboxEventJpaRepository deadRepo;
    private final OutboxMetricsService metricsService;

    public OutboxAdminController(OutboxEventJpaRepository outboxRepo,
                                 DeadOutboxEventJpaRepository deadRepo,
                                 OutboxMetricsService metricsService) {
        this.outboxRepo = outboxRepo;
        this.deadRepo = deadRepo;
        this.metricsService = metricsService;
    }

    @GetMapping("/stats")
    public Map<String, Object> stats() {
        return Map.of(
                "counts", Map.of(
                        OutboxStatus.PENDING.name(), outboxRepo.countByStatus(OutboxStatus.PENDING),
                        OutboxStatus.RETRY.name(), outboxRepo.countByStatus(OutboxStatus.RETRY),
                        OutboxStatus.PROCESSING.name(), outboxRepo.countByStatus(OutboxStatus.PROCESSING),
                        OutboxStatus.SENT.name(), outboxRepo.countByStatus(OutboxStatus.SENT),
                        OutboxStatus.DEAD.name(), outboxRepo.countByStatus(OutboxStatus.DEAD)
                ),
                "due_now", outboxRepo.countByStatuses(List.of(OutboxStatus.PENDING, OutboxStatus.RETRY))
        );
    }

    @GetMapping("/metrics")
    public Map<String, Double> liveMetrics() {
        return metricsService.collectMetrics();
    }

    @GetMapping
    public Page<OutboxEventEntity> browse(
            @Valid @RequestParam(required = false) OutboxStatus status,
            @RequestParam(required = false) String eventType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return outboxRepo.browse(status, eventType, pageable);
    }

    @GetMapping("/dead")
    public Page<DeadOutboxEventEntity> browseDead(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return deadRepo.findAllByOrderByDeadAtDesc(pageable);
    }

    @PostMapping("/requeue/{id}")
    @Transactional
    public Map<String, Object> requeue(@PathVariable UUID id) {
        var e = outboxRepo.findById(id).orElseThrow();
        outboxRepo.updateDeliveryState(e.getId(), OutboxStatus.PENDING, 0, null, null, Instant.now());
        return Map.of("status", "OK", "id", id, "action", "requeue");
    }

    @PostMapping("/requeue-dead/{id}")
    @Transactional
    public Map<String, Object> requeueFromDead(@PathVariable UUID id) {
        var dead = deadRepo.findById(id).orElseThrow();
        outboxRepo.save(toPendingOutbox(dead));
        deadRepo.deleteById(id);
        return Map.of("status", "OK", "id", id, "action", "requeue-dead");
    }

    private OutboxEventEntity toPendingOutbox(DeadOutboxEventEntity dead) {
        OutboxEventEntity e = new OutboxEventEntity();
        e.setId(dead.getId());
        e.setType(dead.getType());
        e.setPayload(dead.getPayload());
        e.setStatus(OutboxStatus.PENDING);
        e.setRetryCount(0);
        e.setNextAttemptAt(null);
        e.setLastError(null);
        e.setCreatedAt(dead.getCreatedAt());
        e.setUpdatedAt(Instant.now());
        return e;
    }
}
