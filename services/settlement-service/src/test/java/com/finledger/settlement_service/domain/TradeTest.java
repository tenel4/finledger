package com.finledger.settlement_service.domain;

import com.finledger.settlement_service.domain.value.Money;
import com.finledger.settlement_service.domain.value.Quantity;
import com.finledger.settlement_service.domain.model.Trade;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class TradeTest {

    private final Quantity quantity = Quantity.of(100);
    private final Money price = Money.of(BigDecimal.valueOf(150.25), "USD");
    private final UUID buyerId = UUID.randomUUID();
    private final UUID sellerId = UUID.randomUUID();
    private final Instant tradeTime = Instant.parse("2025-09-10T10:15:30Z");

    @Test
    void createNew_shouldGenerateIdAndTradeTime() {
        Trade trade = Trade.createNew("AAPL", Trade.Side.BUY, quantity, price, buyerId, sellerId);

        assertThat(trade.id()).isNotNull();
        assertThat(trade.tradeTime()).isNotNull();
        assertThat(trade.symbol()).isEqualTo("AAPL");
        assertThat(trade.side()).isEqualTo(Trade.Side.BUY);
    }

    @Test
    void rehydrate_shouldUseProvidedIdAndTradeTime() {
        UUID id = UUID.randomUUID();
        Trade trade = Trade.rehydrate(id, "AAPL", Trade.Side.SELL, quantity, price, buyerId, sellerId, tradeTime);

        assertThat(trade.id()).isEqualTo(id);
        assertThat(trade.tradeTime()).isEqualTo(tradeTime);
        assertThat(trade.side()).isEqualTo(Trade.Side.SELL);
    }

    @Test
    void shouldThrow_whenBuyerAndSellerAreSame() {
        UUID sameId = UUID.randomUUID();
        assertThatThrownBy(() -> Trade.createNew("AAPL", Trade.Side.BUY, quantity, price, sameId, sameId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Buyer and Seller cannot be the same");
    }

    @Test
    void grossAmount_shouldReturnQuantityTimesPrice() {
        Trade trade = Trade.rehydrate(UUID.randomUUID(), "AAPL", Trade.Side.BUY, quantity, price, buyerId, sellerId, tradeTime);
        assertThat(trade.grossAmount().amount())
                .isEqualByComparingTo(BigDecimal.valueOf(15025.00).setScale(4, RoundingMode.HALF_UP));
    }

    @Test
    void equalsAndHashCode_shouldBeBasedOnId() {
        UUID id = UUID.randomUUID();
        Trade t1 = Trade.rehydrate(id, "AAPL", Trade.Side.BUY, quantity, price, buyerId, sellerId, tradeTime);
        Trade t2 = Trade.rehydrate(id, "AAPL", Trade.Side.SELL, quantity, price, buyerId, sellerId, tradeTime);

        assertThat(t1)
                .isEqualTo(t2)
                .hasSameHashCodeAs(t2);
    }
}

