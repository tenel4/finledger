package com.finledger.settlement_service.application.port.inbound;

import com.finledger.settlement_service.application.port.outbound.EodReconciliationPort;
import java.time.LocalDate;

public interface RunEodReconciliationUseCase {
  EodReconciliationPort.Result execute(LocalDate date);
}
