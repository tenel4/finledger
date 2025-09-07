package com.finledger.settlement_service.application;

import com.finledger.settlement_service.application.dto.CreateTradeRequest;
import com.finledger.settlement_service.application.dto.CreateTradeResponse;

public interface TradeApplicationService {
    CreateTradeResponse createTrade(CreateTradeRequest request);
}
