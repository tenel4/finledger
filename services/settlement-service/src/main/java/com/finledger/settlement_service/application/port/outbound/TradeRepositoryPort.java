package com.finledger.settlement_service.application.port.outbound;

import com.finledger.settlement_service.domain.model.Trade;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TradeRepositoryPort {
    Trade save(Trade trade);
    Optional<Trade> findById(UUID id);
    List<Trade> find(String symbol, Instant from, Instant to, Trade.Side side);
}
