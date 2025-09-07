package com.finledger.settlement_service.domain.model;


import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class Trade {
    private final TradeId id;
    private final String symbol;
    private final Side side;
    private final Quantity quantity;
    private final Money price;
    private final Money notionalAmount;
    private final UUID buyerAccountId;
    private final UUID sellerAccountId;
    private final Instant createdAt;

    private Trade(String symbol, Side side, Quantity quantity, Money price, UUID buyerAccountId, UUID sellerAccountId, Instant createdAt) {
        if (symbol == null || symbol.isBlank()) throw new IllegalArgumentException("Symbol cannot be null or blank");
        if (buyerAccountId == null) throw new IllegalArgumentException("Buyer Account Id cannot be null");
        if (sellerAccountId == null) throw new IllegalArgumentException("Seller Account Id cannot be null or blank");

        this.id = TradeId.newId();
        this.symbol = symbol;
        this.side = side;
        this.quantity = quantity;
        this.price = price;
        this.notionalAmount = price.times(BigDecimal.valueOf(quantity.value()));
        this.buyerAccountId = buyerAccountId;
        this.sellerAccountId = sellerAccountId;
        this.createdAt = createdAt;
    }

    public static Trade of(String symbol, Side side, Quantity quantity, Money price, UUID buyerAccountId, UUID sellerAccountId, Instant createdAt) {
        return new Trade(symbol, side, quantity, price, buyerAccountId, sellerAccountId, createdAt);
    }

    public TradeId id() { return id; }
    public String symbol() { return symbol; }
    public Side side() { return side; }
    public Quantity quantity() { return quantity; }
    public Money price() { return price; }
    public Money notional() { return notionalAmount; }
    public UUID buyerAccountId() { return buyerAccountId; }
    public UUID sellerAccountId() { return sellerAccountId; }
    public Instant createdAt() { return createdAt; }
}
