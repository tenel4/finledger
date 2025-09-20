package com.finledger.settlement_service.adapter.inbound.rest;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finledger.settlement_service.adapter.inbound.rest.dto.CreateTradeRequest;
import com.finledger.settlement_service.application.port.inbound.CreateTradeUseCase;
import com.finledger.settlement_service.application.port.inbound.GetSettlementsUseCase;
import com.finledger.settlement_service.application.port.inbound.GetTradesUseCase;
import com.finledger.settlement_service.application.port.inbound.RunEodReconciliationUseCase;
import com.finledger.settlement_service.application.port.outbound.EodReconciliationPort;
import com.finledger.settlement_service.domain.model.Settlement;
import com.finledger.settlement_service.domain.model.Trade;
import com.finledger.settlement_service.domain.value.Money;
import com.finledger.settlement_service.domain.value.Quantity;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(TradeController.class)
class TradeControllerWebTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @MockitoBean private CreateTradeUseCase createTrade;
  @MockitoBean private GetTradesUseCase getTrades;
  @MockitoBean private GetSettlementsUseCase getSettlements;
  @MockitoBean private RunEodReconciliationUseCase runEod;

  @Test
  void createTrade_shouldReturn201AndResponseBody() throws Exception {
    UUID tradeId = UUID.randomUUID();
    UUID messageId = UUID.randomUUID();
    Instant tradeTime = Instant.parse("2025-09-10T10:15:30Z");

    CreateTradeUseCase.Result result = new CreateTradeUseCase.Result(tradeId, tradeTime, messageId);
    when(createTrade.execute(any())).thenReturn(result);

    CreateTradeRequest req =
        new CreateTradeRequest(
            "AAPL",
            Trade.Side.BUY,
            10,
            BigDecimal.valueOf(150.25),
            "USD",
            UUID.randomUUID(),
            UUID.randomUUID());

    mockMvc
        .perform(
            post("/api/trades")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(tradeId.toString()))
        .andExpect(jsonPath("$.tradeTime").value(tradeTime.toString()))
        .andExpect(jsonPath("$.messageId").value(messageId.toString()));
  }

  @Test
  void getTrades_shouldReturnTrades() throws Exception {
    Trade trade =
        Trade.rehydrate(
            UUID.randomUUID(),
            "AAPL",
            Trade.Side.BUY,
            Quantity.of(10),
            Money.of(BigDecimal.valueOf(150.25), "USD"),
            UUID.randomUUID(),
            UUID.randomUUID(),
            Instant.parse("2025-09-10T10:15:30Z"));
    when(getTrades.execute(any(), any(), any(), any())).thenReturn(List.of(trade));

    mockMvc
        .perform(
            get("/api/trades")
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
    Settlement settlement =
        Settlement.rehydrate(
            UUID.randomUUID(),
            UUID.randomUUID(),
            LocalDate.of(2025, 9, 10),
            Money.of(BigDecimal.valueOf(1000), "USD"),
            Money.of(BigDecimal.valueOf(50), "USD"),
            Money.of(BigDecimal.valueOf(950), "USD"),
            Settlement.Status.PENDING,
            UUID.randomUUID());
    when(getSettlements.execute(any(), any())).thenReturn(List.of(settlement));

    mockMvc
        .perform(get("/api/settlements").param("status", "PENDING").param("date", "2025-09-10"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].status").value("PENDING"));
  }

  @Test
  void runEod_shouldReturnAcceptedAndResult() throws Exception {
    EodReconciliationPort.Result result = new EodReconciliationPort.Result("/tmp/report", 3);
    when(runEod.execute(any())).thenReturn(result);

    mockMvc
        .perform(post("/api/eod/run").param("date", "2025-09-10"))
        .andExpect(status().isAccepted())
        .andExpect(jsonPath("$.reportPath").value("/tmp/report"))
        .andExpect(jsonPath("$.anomalies").value(3));
  }
}
