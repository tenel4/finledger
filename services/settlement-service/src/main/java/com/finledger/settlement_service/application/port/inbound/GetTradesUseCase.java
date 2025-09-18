package com.finledger.settlement_service.application.port.inbound;

import com.finledger.settlement_service.domain.model.Trade;

import java.time.Instant;
import java.util.List;

public interface GetTradesUseCase {
    List<Trade> execute(String symbol, Instant from, Instant to, Trade.Side side);
}
