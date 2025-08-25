package com.finledger.settlement_service.repository;

import com.finledger.settlement_service.model.Trade;
import com.finledger.settlement_service.repository.base.AbstractRepositoryTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.UUID;

import static com.finledger.settlement_service.model.enums.Side.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
class TradeRepositoryTest extends AbstractRepositoryTest {
    @Autowired
    private TradeRepository tradeRepository;

    @Test
    void testSaveAndFindTrade() {
        Trade trade = new Trade();
        trade.setSymbol("AAPL");
        trade.setSide(BUY);
        trade.setQuantity(100L);
        trade.setPrice(BigDecimal.valueOf(185.2));
        trade.setCurrency("USD");
        trade.setBuyerAccountId(UUID.randomUUID());
        trade.setSellerAccountId(UUID.randomUUID());

        Trade saved = tradeRepository.save(trade);
        assertThat(saved.getId()).isNotNull();
    }
}

