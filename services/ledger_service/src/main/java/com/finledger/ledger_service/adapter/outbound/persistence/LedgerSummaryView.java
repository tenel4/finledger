// src/main/java/com/finledger/ledger_service/adapter/outbound/persistence/LedgerSummaryView.java
package com.finledger.ledger_service.adapter.outbound.persistence;

import java.math.BigDecimal;
import java.util.UUID;

public interface LedgerSummaryView {
    UUID getAccountId();
    String getCurrency();
    BigDecimal getSum();
}
