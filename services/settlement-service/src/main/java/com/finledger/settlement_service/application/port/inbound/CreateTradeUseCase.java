package com.finledger.settlement_service.application.port.inbound;

import com.finledger.settlement_service.domain.model.Trade;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public interface CreateTradeUseCase {
  Result execute(Command command);

  record Command(
      String symbol,
      Trade.Side side,
      long quantity,
      BigDecimal price,
      String currency,
      UUID buyerAccountId,
      UUID sellerAccountId) {}

  record Result(UUID id, Instant tradeTime, UUID messageId) {}
}
