package com.finledger.ledger_service.adapter.inbound.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finledger.ledger_service.application.port.inbound.GetLedgerEntriesUseCase;
import com.finledger.ledger_service.application.port.inbound.GetLedgerSummaryUseCase;
import com.finledger.ledger_service.domain.model.LedgerEntry;
import com.finledger.ledger_service.domain.model.LedgerEntryReferenceType;
import com.finledger.ledger_service.domain.value.SignedMoney;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LedgerController.class)
class LedgerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @MockitoBean
    private GetLedgerEntriesUseCase getLedgerEntries;

    @MockitoBean
    private GetLedgerSummaryUseCase getSummary;

    @Test
    void getLedgerEntries_shouldReturnMappedResponses() throws Exception {
        UUID accountId = UUID.randomUUID();
        UUID entryId = UUID.randomUUID();
        UUID referenceId = UUID.randomUUID();
        Instant entryTime = Instant.parse("2025-09-18T10:15:30Z");

        LedgerEntry entry = LedgerEntry.rehydrate(
                entryId,
                entryTime,
                accountId,
                SignedMoney.of(BigDecimal.valueOf(150.25), Currency.getInstance("USD")),
                LedgerEntryReferenceType.TRADE,
                referenceId,
                UUID.randomUUID()
        );

        when(getLedgerEntries.execute(any(), any(), any())).thenReturn(List.of(entry));

        mockMvc.perform(get("/api/ledger")
                        .param("accountId", accountId.toString())
                        .param("from", "2025-09-01")
                        .param("to", "2025-09-30")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(entryId.toString()))
                .andExpect(jsonPath("$[0].entryTime").value(entryTime.toString()))
                .andExpect(jsonPath("$[0].accountId").value(accountId.toString()))
                .andExpect(jsonPath("$[0].currency").value("USD"))
                .andExpect(jsonPath("$[0].amountSigned").value(150.25))
                .andExpect(jsonPath("$[0].referenceType").value("TRADE"))
                .andExpect(jsonPath("$[0].referenceId").value(referenceId.toString()));
    }

    @Test
    void getLedgerSummary_shouldReturnMappedResponses() throws Exception {
        UUID accountId = UUID.randomUUID();

        GetLedgerSummaryUseCase.Result result =
                new GetLedgerSummaryUseCase.Result(accountId, "USD", BigDecimal.valueOf(500.75));

        when(getSummary.execute(any())).thenReturn(List.of(result));

        mockMvc.perform(get("/api/ledger/summary")
                        .param("date", "2025-09-18")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].accountId").value(accountId.toString()))
                .andExpect(jsonPath("$[0].currency").value("USD"))
                .andExpect(jsonPath("$[0].sum").value(500.75));
    }
}
