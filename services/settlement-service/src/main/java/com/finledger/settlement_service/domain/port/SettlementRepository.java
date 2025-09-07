package com.finledger.settlement_service.domain.port;

import com.finledger.settlement_service.domain.model.Settlement;

public interface SettlementRepository {
    Settlement save(Settlement settlement);
}
