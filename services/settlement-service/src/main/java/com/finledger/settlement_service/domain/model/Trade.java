package com.finledger.settlement_service.domain.model;

import com.finledger.settlement_service.domain.value.Money;
import com.finledger.settlement_service.domain.value.Quantity;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class Trade {
  private final UUID id;
  private final String symbol;
  private final Side side;
  private final Quantity quantity;
  private final Money price;
  private final UUID buyerAccountId;
  private final UUID sellerAccountId;
  private final Instant tradeTime;

  public enum Side {
    BUY,
    SELL
  }

  private Trade(
      UUID id,
      String symbol,
      Side side,
      Quantity quantity,
      Money price,
      UUID buyerAccountId,
      UUID sellerAccountId,
      Instant tradeTime) {
    validate(id, symbol, side, quantity, price, buyerAccountId, sellerAccountId, tradeTime);
    this.id = id;
    this.symbol = symbol;
    this.side = side;
    this.quantity = quantity;
    this.price = price;
    this.buyerAccountId = buyerAccountId;
    this.sellerAccountId = sellerAccountId;
    this.tradeTime = tradeTime;
  }

  public static Trade createNew(
      String symbol,
      Side side,
      Quantity quantity,
      Money price,
      UUID buyerAccountId,
      UUID sellerAccountId) {
    return new Trade(
        UUID.randomUUID(),
        symbol,
        side,
        quantity,
        price,
        buyerAccountId,
        sellerAccountId,
        Instant.now());
  }

  public static Trade rehydrate(
      UUID id,
      String symbol,
      Side side,
      Quantity quantity,
      Money price,
      UUID buyerAccountId,
      UUID sellerAccountId,
      Instant tradeTime) {
    return new Trade(id, symbol, side, quantity, price, buyerAccountId, sellerAccountId, tradeTime);
  }

  public Money grossAmount() {
    return price.times(BigDecimal.valueOf(quantity.value()));
  }

  public boolean isBuySide() {
    return side == Side.BUY;
  }

  public boolean isSellSide() {
    return side == Side.SELL;
  }

  public UUID id() {
    return id;
  }

  public String symbol() {
    return symbol;
  }

  public Side side() {
    return side;
  }

  public Quantity quantity() {
    return quantity;
  }

  public Money price() {
    return price;
  }

  public UUID buyerAccountId() {
    return buyerAccountId;
  }

  public UUID sellerAccountId() {
    return sellerAccountId;
  }

  public Instant tradeTime() {
    return tradeTime;
  }

  private static void validate(
      UUID id,
      String symbol,
      Side side,
      Quantity quantity,
      Money price,
      UUID buyerAccountId,
      UUID sellerAccountId,
      Instant tradeTime) {
    Objects.requireNonNull(id, "Trade: ID cannot be null");
    if (symbol == null || symbol.isBlank()) {
      throw new IllegalArgumentException("Trade: Symbol cannot be null or blank");
    }
    Objects.requireNonNull(side, "Trade: Side cannot be null");
    Objects.requireNonNull(quantity, "Trade: Quantity cannot be null");
    Objects.requireNonNull(price, "Trade: Price cannot be null");
    Objects.requireNonNull(buyerAccountId, "Trade: Buyer Account Id cannot be null");
    Objects.requireNonNull(sellerAccountId, "Trade: Seller Account Id cannot be null");
    if (buyerAccountId.equals(sellerAccountId)) {
      throw new IllegalArgumentException("Trade: Buyer and Seller cannot be the same");
    }
    Objects.requireNonNull(tradeTime, "Trade: Trade Time cannot be null");
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Trade trade)) return false;
    return id.equals(trade.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }
}
