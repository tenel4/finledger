package com.finledger.settlement_service.domain.port;

import com.finledger.settlement_service.domain.model.Trade;

public interface TradeRepository {
    Trade save(Trade trade);
}
