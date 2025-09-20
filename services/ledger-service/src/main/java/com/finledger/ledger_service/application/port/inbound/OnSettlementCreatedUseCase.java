package com.finledger.ledger_service.application.port.inbound;

import java.math.BigDecimal;
import java.util.UUID;

public interface OnSettlementCreatedUseCase {
  void execute(
      UUID messageKey,
      UUID settlementId,
      UUID buyerAccountId,
      UUID sellerAccountId,
      BigDecimal netAmount,
      String currency);
}
