package com.finledger.settlement_service.infrastructure.persistance.repository;

import com.finledger.settlement_service.domain.model.Trade;
import com.finledger.settlement_service.domain.port.TradeRepository;
import com.finledger.settlement_service.infrastructure.persistance.entity.TradeEntity;
import org.springframework.stereotype.Component;

@Component
public class TradeRepositoryAdapter implements TradeRepository {
    private final TradeEntityJpaRepository repo;

    public TradeRepositoryAdapter(TradeEntityJpaRepository repo) {
        this.repo = repo;
    }

    @Override
    public Trade save(Trade trade) {
        var entity = new TradeEntity();
        entity.setId(trade.id().value());
        entity.setSymbol(trade.symbol());
        entity.setSide(trade.side());
        entity.setQuantity(trade.quantity().value());
        entity.setPrice(trade.price().amount());
        entity.setCurrency(trade.price().currency());
        entity.setNotionalAmount(trade.notional().amount());
        entity.setBuyerAccountId(trade.buyerAccountId());
        entity.setSellerAccountId(trade.sellerAccountId());
        entity.setCreatedAt(trade.createdAt());

        repo.save(entity);

        return trade;
    }
}
