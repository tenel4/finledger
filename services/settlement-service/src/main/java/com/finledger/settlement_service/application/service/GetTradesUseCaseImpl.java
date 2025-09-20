package com.finledger.settlement_service.application.service;

import com.finledger.settlement_service.application.port.inbound.GetTradesUseCase;
import com.finledger.settlement_service.application.port.outbound.TradeRepositoryPort;
import com.finledger.settlement_service.domain.model.Trade;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class GetTradesUseCaseImpl implements GetTradesUseCase {
  private final TradeRepositoryPort tradeRepositoryPort;

  public GetTradesUseCaseImpl(TradeRepositoryPort tradeRepositoryPort) {
    this.tradeRepositoryPort = tradeRepositoryPort;
  }

  public List<Trade> execute(String symbol, Instant from, Instant to, Trade.Side side) {
    return tradeRepositoryPort.find(symbol, from, to, side);
  }
}
