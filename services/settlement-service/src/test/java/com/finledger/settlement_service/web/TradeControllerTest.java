package com.finledger.settlement_service.web;

import com.finledger.settlement_service.application.dto.CreateTradeRequest;
import com.finledger.settlement_service.application.dto.CreateTradeResponse;
import com.finledger.settlement_service.domain.model.Side;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class TradeControllerTest {
    @Test
    void returnsCreated() {
        var controller = new TradeController(req -> new CreateTradeResponse(UUID.randomUUID(), "mk"));
        var response = controller.createTrade(new CreateTradeRequest("AAPL", Side.BUY, 1L, BigDecimal.ONE, "USD", UUID.randomUUID(), UUID.randomUUID()));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Assertions.assertNotNull(response.getBody());
        assertThat(response.getBody().messageKey()).isEqualTo("mk");
    }
}
