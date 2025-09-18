package com.finledger.settlement_service.application.port.outbound;

import java.time.LocalDate;

public interface EodReconciliationPort {
    record Result(String reportPath, int anomalies) {}
    Result runAndExport(LocalDate date);
}
