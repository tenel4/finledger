package com.finledger.settlement_service.adapter.outbound.persistance.repository;

import com.finledger.settlement_service.adapter.outbound.persistance.entity.TradeEntity;
import com.finledger.settlement_service.domain.value.Money;
import com.finledger.settlement_service.domain.value.Quantity;
import com.finledger.settlement_service.domain.model.Trade;
import com.finledger.settlement_service.application.port.outbound.TradeRepositoryPort;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class TradeRepositoryAdapter implements TradeRepositoryPort {
    private final TradeJpaRepository repo;

    public TradeRepositoryAdapter(TradeJpaRepository repo) {
        this.repo = repo;
    }

    @Override
    @Transactional
    public Trade save(Trade trade) {
        TradeEntity entity = toEntity(trade);
        repo.save(entity);
        return trade;
    }

    @Override
    public Optional<Trade> findById(UUID id) {
        return repo.findById(id).map(TradeRepositoryAdapter::toDomain);
    }

    @Override
    public List<Trade> find(String symbol, Instant from, Instant to, Trade.Side side) {
        return repo.search(symbol, from, to, side)
                .stream().map(TradeRepositoryAdapter::toDomain).toList();
    }

    private static TradeEntity toEntity(Trade t) {
        TradeEntity entity = new TradeEntity();
        entity.setId(t.id());
        entity.setSymbol(t.symbol());
        entity.setSide(t.side());
        entity.setQuantity(t.quantity().value());
        entity.setPrice(t.price().amount());
        entity.setCurrency(t.price().currency().toString());
        entity.setBuyerAccountId(t.buyerAccountId());
        entity.setSellerAccountId(t.sellerAccountId());
        entity.setTradeTime(t.tradeTime());
        return entity;
    }

    private static Trade toDomain(TradeEntity e) {
        Quantity quantity = Quantity.of(e.getQuantity());
        Money price = Money.of(e.getPrice(), e.getCurrency());
        return Trade.rehydrate(e.getId(), e.getSymbol(), e.getSide(), quantity,
                price, e.getBuyerAccountId(), e.getSellerAccountId(), e.getTradeTime());
    }
}
