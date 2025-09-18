package com.finledger.settlement_service.adapter.inbound.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finledger.settlement_service.adapter.inbound.rest.dto.CreateTradeRequest;
import com.finledger.settlement_service.application.port.inbound.CreateTradeUseCase;
import com.finledger.settlement_service.application.port.inbound.GetSettlementsUseCase;
import com.finledger.settlement_service.application.port.inbound.GetTradesUseCase;
import com.finledger.settlement_service.application.port.inbound.RunEodReconciliationUseCase;
import com.finledger.settlement_service.domain.value.Money;
import com.finledger.settlement_service.domain.value.Quantity;
import com.finledger.settlement_service.domain.model.Settlement;
import com.finledger.settlement_service.domain.model.Trade;
import com.finledger.settlement_service.application.port.outbound.EodReconciliationPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = RANDOM_PORT)
class TradeControllerIT {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    @MockitoSpyBean
    private CreateTradeUseCase createTrade;
    @MockitoSpyBean
    private GetTradesUseCase getTrades;
    @MockitoSpyBean
    private GetSettlementsUseCase getSettlements;
    @MockitoSpyBean
    private RunEodReconciliationUseCase runEod;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    void createTrade_shouldReturn201AndBody() throws Exception {
        UUID tradeId = UUID.randomUUID();
        UUID messageId = UUID.randomUUID();
        Instant tradeTime = Instant.parse("2025-09-10T10:15:30Z");

        doReturn(new CreateTradeUseCase.Result(tradeId, tradeTime, messageId))
                .when(createTrade).execute(any());

        CreateTradeRequest req = new CreateTradeRequest(
                "AAPL",
                Trade.Side.BUY,
                10,
                BigDecimal.valueOf(150.25),
                "USD",
                UUID.randomUUID(),
                UUID.randomUUID()
        );

        mockMvc.perform(post("/api/trades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(tradeId.toString()))
                .andExpect(jsonPath("$.tradeTime").value(tradeTime.toString()))
                .andExpect(jsonPath("$.messageId").value(messageId.toString()));
    }

    @Test
    void getTrades_shouldReturnTrades() throws Exception {
        Trade trade = Trade.rehydrate(
                UUID.randomUUID(),
                "AAPL",
                Trade.Side.BUY,
                Quantity.of(10),
                Money.of(BigDecimal.valueOf(150.25), "USD"),
                UUID.randomUUID(),
                UUID.randomUUID(),
                Instant.parse("2025-09-10T10:15:30Z")
        );
        doReturn(List.of(trade)).when(getTrades).execute(any(), any(), any(), any());

        mockMvc.perform(get("/api/trades")
                        .param("symbol", "AAPL")
                        .param("from", "2025-09-01")
                        .param("to", "2025-09-30")
                        .param("side", "BUY"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].symbol").value("AAPL"))
                .andExpect(jsonPath("$[0].side").value("BUY"));
    }

    @Test
    void getSettlements_shouldReturnSettlements() throws Exception {
        Settlement settlement = Settlement.rehydrate(
                UUID.randomUUID(),
                UUID.randomUUID(),
                LocalDate.of(2025, 9, 10),
                Money.of(BigDecimal.valueOf(1000), "USD"),
                Money.of(BigDecimal.valueOf(50), "USD"),
                Money.of(BigDecimal.valueOf(950), "USD"),
                Settlement.Status.PENDING,
                UUID.randomUUID()
        );
        doReturn(List.of(settlement)).when(getSettlements).execute(any(), any());

        mockMvc.perform(get("/api/settlements")
                        .param("status", "PENDING")
                        .param("date", "2025-09-10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("PENDING"));
    }

    @Test
    void runEod_shouldReturnAcceptedAndResult() throws Exception {
        EodReconciliationPort.Result result =
                new EodReconciliationPort.Result("/tmp/report", 3);
        doReturn(result).when(runEod).execute(any());

        mockMvc.perform(post("/api/eod/run").param("date", "2025-09-10"))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.reportPath").value("/tmp/report"))
                .andExpect(jsonPath("$.anomalies").value(3));
    }

    @Test
    void getTrades_returnsEmptyArray() throws Exception {
        when(getTrades.execute(any(), any(), any(), any())).thenReturn(List.of());

        mockMvc.perform(get("/api/trades")
                        .param("symbol", "AAPL")
                        .param("from", "2025-09-01")
                        .param("to", "2025-09-30")
                        .param("side", "BUY"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void createTrade_withInvalidBody_returns400() throws Exception {
        // Missing required fields
        mockMvc.perform(post("/api/trades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }
}
