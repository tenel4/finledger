package com.finledger.settlement_service.adapter.inbound.rest.mapper;

import com.finledger.settlement_service.adapter.inbound.rest.dto.GetTradesResponse;
import com.finledger.settlement_service.domain.model.Trade;

public class TradeMapper {
    private TradeMapper() {
        // utility class
    }

    public static GetTradesResponse toResponse(Trade t) {
        return new GetTradesResponse(
                t.id(),
                t.symbol(),
                t.side(),
                t.quantity().value(),
                t.price().amount(),
                t.price().currency().toString(),
                t.buyerAccountId(),
                t.sellerAccountId(),
                t.tradeTime()
        );
    }
}
