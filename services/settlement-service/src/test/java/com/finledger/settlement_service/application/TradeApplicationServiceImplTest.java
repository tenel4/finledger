package com.finledger.settlement_service.application;

import com.finledger.settlement_service.application.dto.CreateTradeRequest;
import com.finledger.settlement_service.application.impl.TradeApplicationServiceImpl;
import com.finledger.settlement_service.domain.model.Side;
import com.finledger.settlement_service.domain.model.Trade;
import com.finledger.settlement_service.domain.port.TradePublisher;
import com.finledger.settlement_service.domain.port.TradeRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class TradeApplicationServiceImplTest {
    @Test
    void createTradePublishesWithCorrelation() {
        AtomicReference<Trade> published = new AtomicReference<>();
        TradeRepository repo = t -> t;
        TradePublisher pub = (t, mid, cid) -> {
            published.set(t);
            assertThat(mid).isNotBlank();
            assertThat(cid).isNotBlank();
        };
        var service = new TradeApplicationServiceImpl(repo, pub);
        var response = service.createTrade(new CreateTradeRequest("AAPL", Side.BUY, 10L, new BigDecimal("100.00"), "USD", UUID.randomUUID(), UUID.randomUUID()));
        assertThat(response.tradeId()).isNotNull();
        assertThat(response.messageKey()).isNotBlank();
        assertThat(published.get()).isNotNull();
    }
}
