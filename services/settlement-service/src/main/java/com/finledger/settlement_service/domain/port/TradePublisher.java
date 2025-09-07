package com.finledger.settlement_service.domain.port;

import com.finledger.settlement_service.domain.model.Trade;

public interface TradePublisher {
    void publishTradeCreated(Trade trade, String messageId, String correlationId);
}
