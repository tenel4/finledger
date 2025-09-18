package com.finledger.settlement_service.adapter.outbound.persistance.repository;

import com.finledger.settlement_service.adapter.outbound.persistance.entity.OutboxEventEntity;
import com.finledger.settlement_service.domain.model.OutboxStatus;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface OutboxEventJpaRepository extends JpaRepository<OutboxEventEntity, UUID> {

    /**
     * Locks and returns a batch of due events for processing.
     */
    @Query(value = """
        SELECT * FROM settlement.outbox_event
         WHERE status IN ('PENDING','RETRY')
           AND (next_attempt_at IS NULL OR next_attempt_at <= NOW())
         ORDER BY created_at
         LIMIT :batchSize
         FOR UPDATE SKIP LOCKED
        """, nativeQuery = true)
    List<OutboxEventEntity> lockDueBatch(@Param("batchSize") int batchSize);

    /**
     * Counts events by a single status.
     */
    long countByStatus(OutboxStatus status);

    /**
     * Counts events by multiple statuses.
     */
    @Query("select count(e) from OutboxEventEntity e where e.status in :statuses")
    long countByStatuses(@Param("statuses") List<OutboxStatus> statuses);

    /**
     * Finds events by status ordered by creation time.
     */
    Page<OutboxEventEntity> findByStatusOrderByCreatedAtAsc(OutboxStatus status, Pageable pageable);

    /**
     * Browses events with optional status and type filters.
     */
    @Query("""
        select e from OutboxEventEntity e
         where (:status is null or e.status = :status)
           and (:type is null or e.type = :type)
         order by e.createdAt desc
        """)
    Page<OutboxEventEntity> browse(@Param("status") OutboxStatus status,
                                   @Param("type") String type,
                                   Pageable pageable);

    /**
     * Updates delivery state for an event.
     */
    @Modifying
    @Transactional
    @Query("""
        UPDATE OutboxEventEntity e
           SET e.status = :status,
               e.retryCount = :retry,
               e.nextAttemptAt = :next,
               e.lastError = :err,
               e.updatedAt = :now
         WHERE e.id = :id
        """)
    int updateDeliveryState(@Param("id") UUID id,
                            @Param("status") OutboxStatus status,
                            @Param("retry") int retry,
                            @Param("next") Instant nextAttemptAt,
                            @Param("err") String lastError,
                            @Param("now") Instant now);

    /**
     * Updates status for an event.
     */
    @Modifying
    @Transactional
    @Query("""
        UPDATE OutboxEventEntity e
           SET e.status = :status,
               e.updatedAt = :now
         WHERE e.id = :id
        """)
    int updateStatus(@Param("id") UUID id,
                     @Param("status") OutboxStatus status,
                     @Param("now") Instant now);
}
