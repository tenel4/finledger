package com.finledger.settlement_service.application.port.inbound;

import com.finledger.settlement_service.application.dto.TradeCreatedEventDto;

public interface OnTradeCreatedUseCase {
    void execute(TradeCreatedEventDto event);
}
