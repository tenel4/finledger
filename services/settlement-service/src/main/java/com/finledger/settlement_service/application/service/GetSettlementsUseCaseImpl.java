package com.finledger.settlement_service.application.service;

import com.finledger.settlement_service.application.port.inbound.GetSettlementsUseCase;
import com.finledger.settlement_service.domain.model.Settlement;
import com.finledger.settlement_service.application.port.outbound.SettlementRepositoryPort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class GetSettlementsUseCaseImpl implements GetSettlementsUseCase {
    private final SettlementRepositoryPort settlementRepositoryPort;

    public GetSettlementsUseCaseImpl(SettlementRepositoryPort settlementRepositoryPort) {
        this.settlementRepositoryPort = settlementRepositoryPort;
    }

    public List<Settlement> execute(Settlement.Status status, LocalDate date) {
        return settlementRepositoryPort.find(status, date);
    }
}
