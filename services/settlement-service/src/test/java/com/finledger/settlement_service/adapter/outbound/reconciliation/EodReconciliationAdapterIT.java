package com.finledger.settlement_service.adapter.outbound.reconciliation;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.finledger.settlement_service.application.port.outbound.EodReconciliationPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class EodReconciliationAdapterIT {

    @Container
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:15")
                    .withDatabaseName("testdb")
                    .withUsername("test")
                    .withPassword("test");

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private ObjectMapper mapper;

    @TempDir
    static Path tempDir;

    private EodReconciliationAdapter adapter;

    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        registry.add("finledger.reports.base-dir", () -> tempDir.toString());
    }

    @BeforeEach
    void setUp() {
        adapter = new EodReconciliationAdapter(jdbc, mapper, tempDir);

        jdbc.execute("CREATE SCHEMA IF NOT EXISTS ledger");
        jdbc.execute("CREATE SCHEMA IF NOT EXISTS settlement");

        jdbc.execute("DROP TABLE IF EXISTS ledger.ledger_entry");
        jdbc.execute("CREATE TABLE ledger.ledger_entry (" +
                "id UUID PRIMARY KEY, currency VARCHAR(3), amount_signed DECIMAL)");

        jdbc.execute("DROP TABLE IF EXISTS settlement.settlement");
        jdbc.execute("CREATE TABLE settlement.settlement (" +
                "id UUID PRIMARY KEY, trade_id UUID, value_date DATE, status VARCHAR(20))");
    }

    @Test
    void runAndExport_detectsAnomaliesAndWritesReports() throws Exception {
        LocalDate date = LocalDate.of(2025, 9, 16);
        UUID ledgerId = UUID.randomUUID();
        jdbc.update("INSERT INTO ledger.ledger_entry (id, currency, amount_signed) VALUES (?, ?, ?)",
                ledgerId, "USD", 100);

        UUID settlementId = UUID.randomUUID();
        UUID tradeId = UUID.randomUUID();
        jdbc.update("INSERT INTO settlement.settlement (id, trade_id, value_date, status) " +
                        "VALUES (?, ?, ?, ?)",
                settlementId, tradeId, date.minusDays(1), "PENDING");

        EodReconciliationPort.Result result = adapter.runAndExport(date);

        assertThat(result.anomalies()).isEqualTo(2);
        assertThat(result.reportPath()).contains(date.toString());

        Path reportDir = Path.of(result.reportPath());
        Path jsonFile = reportDir.resolve("recon.json");
        Path csvFile = reportDir.resolve("anomalies.csv");

        assertThat(Files.exists(jsonFile)).isTrue();
        assertThat(Files.exists(csvFile)).isTrue();

        List<Map<String, Object>> anomalies =
                mapper.readValue(jsonFile.toFile(), new TypeReference<>() {});
        assertThat(anomalies).hasSize(2);
        assertThat(anomalies.getFirst()).containsKey("type");

        String csvContent = Files.readString(csvFile);
        assertThat(csvContent)
                .contains("TRIAL_BALANCE_MISMATCH")
                .contains("UNSETTLED_PAST_DUE");
    }
}
