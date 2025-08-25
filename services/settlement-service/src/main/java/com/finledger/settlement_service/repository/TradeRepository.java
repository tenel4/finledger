package com.finledger.settlement_service.repository;

import com.finledger.settlement_service.model.Trade;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TradeRepository extends JpaRepository<Trade, UUID> {
    List<Trade> findBySymbol(String symbol);
}
