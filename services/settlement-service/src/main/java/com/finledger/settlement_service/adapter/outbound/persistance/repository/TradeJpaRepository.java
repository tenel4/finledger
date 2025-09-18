package com.finledger.settlement_service.adapter.outbound.persistance.repository;

import com.finledger.settlement_service.adapter.outbound.persistance.entity.TradeEntity;
import com.finledger.settlement_service.domain.model.Trade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface TradeJpaRepository extends JpaRepository<TradeEntity, UUID> {
    @Query("""
        SELECT e FROM TradeEntity e
        WHERE e.symbol = :symbol
          AND e.tradeTime >= :from
          AND e.tradeTime < :to
          AND e.side = :side
        ORDER BY e.tradeTime ASC
    """)
    List<TradeEntity> search(@Param("symbol") String symbol,
                             @Param("from") Instant from,
                             @Param("to") Instant to,
                             @Param("side") Trade.Side side);

}
