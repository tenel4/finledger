package com.finledger.settlement_service.controller;

import com.finledger.settlement_service.model.dto.CreateTradeRequest;
import com.finledger.settlement_service.model.dto.CreateTradeResponse;
import com.finledger.settlement_service.service.TradeService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.Instant;

import static com.finledger.settlement_service.model.enums.Side.*;
import static java.util.UUID.randomUUID;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TradeController.class)
@Import(TradeControllerTest.TestConfig.class)
class TradeControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private TradeService tradeService;

    static class TestConfig {
        @Bean
        public TradeService tradeService() {
            return Mockito.mock(TradeService.class);
        }
    }

    @Test
    void createTrade_shouldReturn400BadRequest_whenFieldsInvalid() throws Exception {
        CreateTradeRequest[] invalidRequests = new CreateTradeRequest[]{
                new CreateTradeRequest(null, BUY, 100L, null, "USD", randomUUID(), randomUUID()),
                new CreateTradeRequest("AAPL", null, 100L, null, "USD", randomUUID(), randomUUID()),
                new CreateTradeRequest("AAPL", BUY, 0L, null, "USD", randomUUID(), randomUUID()),
                new CreateTradeRequest("AAPL", BUY, -10L, null, "USD", randomUUID(), randomUUID()),
                new CreateTradeRequest("AAPL", BUY, 100L, null, null, randomUUID(), randomUUID()),
                new CreateTradeRequest("AAPL", BUY, 100L, null, "USD", randomUUID(), randomUUID()),
                new CreateTradeRequest("AAPL", BUY, 100L, null, "USD", randomUUID(), randomUUID())
        };

        for (CreateTradeRequest invalidRequest : invalidRequests) {
            mockMvc.perform(post("/api/trades")
                            .contentType(APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Test
    void createTrade_shouldReturn400BadRequest_whenSideIsInvalid() throws Exception {
        String invalidRequestJson = """
                {
                    "symbol": "AAPL",
                    "side": "B",
                    "quantity": 100,
                    "price": 785.00,
                    "currency": "USD",
                    "buyerAccountId": "550e8400-e29b-41d4-a716-446655440000",
                    "sellerAccountId": "550e8400-e29b-41d4-a716-446655440001"
                }
                """;
        mockMvc.perform(post("/api/trades")
                        .contentType(APPLICATION_JSON)
                        .content(invalidRequestJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createTrade_shouldReturn201_whenRequestIsValid() throws Exception {
        CreateTradeRequest validRequest = new CreateTradeRequest("AAPL", BUY, 100L, BigDecimal.valueOf(785.00), "USD", randomUUID(), randomUUID());
        CreateTradeResponse mockResponse = new CreateTradeResponse(randomUUID(), Instant.now(), randomUUID());
        Mockito.when(tradeService.createTrade(Mockito.any(CreateTradeRequest.class))).thenReturn(mockResponse);
        mockMvc.perform(post("/api/trades")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated());
    }
}