package com.finledger.settlement_service.adapter.inbound.rest.mapper;

import com.finledger.settlement_service.adapter.inbound.rest.dto.GetSettlementsResponse;
import com.finledger.settlement_service.domain.model.Settlement;

public class SettlementMapper {

    private SettlementMapper() {
        // utility class
    }

    public static GetSettlementsResponse toResponse(Settlement s) {
        return new GetSettlementsResponse(
                s.id(),
                s.tradeId(),
                s.valueDate(),
                s.grossAmount().amount(),
                s.fees().amount(),
                s.netAmount().amount(),
                s.grossAmount().currency().toString(),
                s.status(),
                s.messageId()
        );
    }
}
