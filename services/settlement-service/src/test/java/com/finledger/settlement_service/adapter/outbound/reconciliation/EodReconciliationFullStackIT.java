package com.finledger.settlement_service.adapter.outbound.reconciliation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@SpringBootTest(webEnvironment = RANDOM_PORT)
class EodReconciliationFullStackIT {

  @Container
  static PostgreSQLContainer<?> postgres =
      new PostgreSQLContainer<>("postgres:16-alpine")
          .withDatabaseName("testdb")
          .withUsername("test")
          .withPassword("test");

  @TempDir static Path reportsDir;

  @DynamicPropertySource
  static void datasourceProps(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", postgres::getJdbcUrl);
    registry.add("spring.datasource.username", postgres::getUsername);
    registry.add("spring.datasource.password", postgres::getPassword);
    registry.add("spring.jpa.hibernate.ddl-auto", () -> "update");
    // point adapter to our temp reports directory
    registry.add("finledger.reports.base-dir", () -> reportsDir.toString());
  }

  @Autowired private JdbcTemplate jdbc;

  @Autowired private TestRestTemplate rest;

  @Autowired private ObjectMapper mapper;

  @BeforeEach
  void prepareSchema() {
    jdbc.execute(
        """
            CREATE SCHEMA IF NOT EXISTS ledger;
            CREATE SCHEMA IF NOT EXISTS settlement;
        """);

    jdbc.execute("CREATE EXTENSION IF NOT EXISTS \"uuid-ossp\"");

    jdbc.execute("DROP TABLE IF EXISTS ledger.ledger_entry");
    jdbc.execute(
        "CREATE TABLE ledger.ledger_entry ("
            + "id UUID DEFAULT gen_random_uuid(), currency VARCHAR(3), amount_signed NUMERIC)");

    jdbc.execute("DROP TABLE IF EXISTS settlement.settlement");
    jdbc.execute(
        "CREATE TABLE settlement.settlement ("
            + "id UUID, trade_id UUID, value_date DATE, status VARCHAR(20))");
  }

  @Test
  void runEod_endToEnd_createsFiles_andDetectsAnomalies() throws Exception {
    // Insert trial balance mismatch
    jdbc.update(
        "INSERT INTO ledger.ledger_entry (currency, amount_signed) VALUES (?, ?)",
        "USD",
        BigDecimal.valueOf(123.45));

    // Insert unsettled past due
    UUID settlementId = UUID.randomUUID();
    UUID tradeId = UUID.randomUUID();
    LocalDate pastDate = LocalDate.now().minusDays(3);
    jdbc.update(
        "INSERT INTO settlement.settlement (id, trade_id, value_date, status) VALUES (?, ?, ?, ?)",
        settlementId,
        tradeId,
        pastDate,
        "PENDING");

    // Call the endpoint
    LocalDate runDate = LocalDate.now();
    ResponseEntity<Map> res = rest.postForEntity("/api/eod/run?date=" + runDate, null, Map.class);

    assertThat(res.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
    assertThat(res.getBody()).isNotNull();

    String reportPath = (String) res.getBody().get("reportPath");
    Integer anomalies = (Integer) res.getBody().get("anomalies");

    assertThat(anomalies).isEqualTo(2);
    assertThat(reportPath).isNotBlank();

    // Validate files and contents
    File jsonFile = Path.of(reportPath).resolve("recon.json").toFile();
    File csvFile = Path.of(reportPath).resolve("anomalies.csv").toFile();
    assertThat(jsonFile).exists().isFile();
    assertThat(csvFile).exists().isFile();

    List<Map<String, Object>> anomalyList = mapper.readValue(jsonFile, new TypeReference<>() {});
    assertThat(anomalyList)
        .extracting(a -> (String) a.get("type"))
        .contains("TRIAL_BALANCE_MISMATCH", "UNSETTLED_PAST_DUE");
  }
}
