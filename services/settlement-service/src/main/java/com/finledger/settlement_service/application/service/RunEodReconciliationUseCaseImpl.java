package com.finledger.settlement_service.application.service;

import com.finledger.settlement_service.application.port.inbound.RunEodReconciliationUseCase;
import com.finledger.settlement_service.application.port.outbound.EodReconciliationPort;
import java.time.LocalDate;
import org.springframework.stereotype.Service;

@Service
public class RunEodReconciliationUseCaseImpl implements RunEodReconciliationUseCase {
  private final EodReconciliationPort eodReconciliation;

  public RunEodReconciliationUseCaseImpl(EodReconciliationPort eodReconciliation) {
    this.eodReconciliation = eodReconciliation;
  }

  public EodReconciliationPort.Result execute(LocalDate date) {
    return eodReconciliation.runAndExport(date);
  }
}
