package com.finledger.settlement_service.adapter.outbound.reconciliation;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.finledger.settlement_service.application.port.outbound.EodReconciliationPort;
import com.finledger.settlement_service.common.exception.EodReconciliationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class EodReconciliationAdapterTest {

    @Mock
    private JdbcTemplate jdbc;

    private ObjectMapper mapper;

    private EodReconciliationAdapter adapter;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mapper = new ObjectMapper()
                .registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule())
                .disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        adapter = new EodReconciliationAdapter(jdbc, mapper, tempDir);
    }

    @Test
    void runAndExport_shouldWriteReportsAndReturnResult() throws Exception {
        LocalDate date = LocalDate.of(2025, 9, 16);

        // Mock trial balance with a mismatch
        when(jdbc.queryForList(anyString()))
                .thenReturn(List.of(Map.of("currency", "USD", "total", BigDecimal.TEN)));

        // Mock unsettled query
        when(jdbc.queryForList(anyString(), ArgumentMatchers.<Object>any()))
                .thenReturn(List.of(Map.of("id", "settle-1",
                        "trade_id", "trade-1",
                        "value_date", LocalDate.now().minusDays(1),
                        "status", "PENDING")));

        // Execute
        EodReconciliationPort.Result result = adapter.runAndExport(date);

        // Verify result
        assertThat(result.reportPath()).contains(date.toString());
        assertThat(result.anomalies()).isEqualTo(2); // one mismatch + one unsettled

        // Verify JSON file exists and contains anomalies
        Path jsonFile = tempDir.resolve(date.toString()).resolve("recon.json");
        assertThat(Files.exists(jsonFile)).isTrue();

        List<Map<String, Object>> anomalies =
                mapper.readValue(jsonFile.toFile(), new TypeReference<>() {});
        assertThat(anomalies).hasSize(2);
        assertThat(anomalies.getFirst()).containsEntry("type", "TRIAL_BALANCE_MISMATCH");

        // Verify CSV file exists
        Path csvFile = tempDir.resolve(date.toString()).resolve("anomalies.csv");
        assertThat(Files.exists(csvFile)).isTrue();
        String csvContent = Files.readString(csvFile);
        assertThat(csvContent)
                .contains("TRIAL_BALANCE_MISMATCH")
                .contains("UNSETTLED_PAST_DUE");
    }

    @Test
    void runAndExport_shouldThrowEodReconciliationException_whenIOExceptionOccurs() throws Exception {
        LocalDate date = LocalDate.of(2025, 9, 16);

        // Force mapper to throw IOException
        ObjectMapper badMapper = mock(ObjectMapper.class);
        ObjectWriter writer = mock(ObjectWriter.class);

        when(badMapper.writerWithDefaultPrettyPrinter()).thenReturn(writer);
        doThrow(new IOException("boom")).when(writer).writeValue(any(File.class), any());

        EodReconciliationAdapter badAdapter = new EodReconciliationAdapter(jdbc, badMapper, tempDir);

        assertThatThrownBy(() -> badAdapter.runAndExport(date))
                .isInstanceOf(EodReconciliationException.class)
                .hasMessageContaining("Failed to export EOD report");
    }
}

