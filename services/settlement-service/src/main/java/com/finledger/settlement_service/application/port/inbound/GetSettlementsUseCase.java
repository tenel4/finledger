package com.finledger.settlement_service.application.port.inbound;

import com.finledger.settlement_service.domain.model.Settlement;

import java.time.LocalDate;
import java.util.List;

public interface GetSettlementsUseCase {
    List<Settlement> execute(Settlement.Status status, LocalDate date);
}
