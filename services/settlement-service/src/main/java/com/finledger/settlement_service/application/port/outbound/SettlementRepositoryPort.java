package com.finledger.settlement_service.application.port.outbound;

import com.finledger.settlement_service.domain.model.Settlement;

import java.time.LocalDate;
import java.util.List;

public interface SettlementRepositoryPort {
    Settlement save(Settlement settlement);
    List<Settlement> find(Settlement.Status status, LocalDate date);
}
