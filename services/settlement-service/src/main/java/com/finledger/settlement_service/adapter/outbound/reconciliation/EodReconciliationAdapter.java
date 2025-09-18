package com.finledger.settlement_service.adapter.outbound.reconciliation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finledger.settlement_service.application.port.outbound.EodReconciliationPort;
import com.finledger.settlement_service.common.exception.EodReconciliationException;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class EodReconciliationAdapter implements EodReconciliationPort {
    // Column / field keys used in anomalies
    private static final String KEY_TYPE = "type";
    private static final String KEY_CURRENCY = "currency";
    private static final String KEY_TOTAL = "total";
    private static final String KEY_SETTLEMENT_ID = "settlementId";
    private static final String KEY_TRADE_ID = "tradeId";
    private static final String KEY_VALUE_DATE = "valueDate";
    private static final String KEY_STATUS = "status";

    // Anomaly types
    private static final String TYPE_TRIAL_BALANCE_MISMATCH = "TRIAL_BALANCE_MISMATCH";
    private static final String TYPE_UNSETTLED_PAST_DUE = "UNSETTLED_PAST_DUE";

    // File names
    private static final String JSON_FILE_NAME = "recon.json";
    private static final String CSV_FILE_NAME = "anomalies.csv";

    // CSV format (built once)
    private static final CSVFormat CSV_FORMAT = CSVFormat.DEFAULT.builder()
            .setHeader(KEY_TYPE, KEY_CURRENCY, KEY_TOTAL, KEY_SETTLEMENT_ID, KEY_TRADE_ID, KEY_VALUE_DATE, KEY_STATUS)
            .build();

    private static final String TRIAL_BALANCE_SQL = """
            SELECT currency, SUM(amount_signed) AS total
            FROM ledger.ledger_entry
            GROUP BY currency
            """;

    private static final String UNSETTLED_SQL = """
            SELECT s.id, s.trade_id, s.value_date, s.status
            FROM settlement.settlement s
            WHERE s.status <> 'SETTLED' AND s.value_date < ?
            """;

    private final JdbcTemplate jdbc;
    private final ObjectMapper mapper;
    private final Path reportsBaseDir;

    public EodReconciliationAdapter(JdbcTemplate jdbc, ObjectMapper mapper,
                                    @Value("${finledger.reports.base-dir}") Path reportsBaseDir) {
        this.jdbc = jdbc;
        this.mapper = mapper;
        this.reportsBaseDir = reportsBaseDir;
    }

    @Override
    public Result runAndExport(LocalDate date) {
        try {
            // 1. Run queries
            List<Map<String, Object>> trialBalance = fetchTrialBalance();
            List<Map<String, Object>> unsettled = fetchUnsettled(date);

            // 2. Detect anomalies
            List<Map<String, Object>> anomalies = detectAnomalies(trialBalance, unsettled);

            // 3. Prepare output directory
            Path reportDir = prepareOutputDir(date);

            // 4. Write JSON
            writeJsonReport(reportDir, anomalies);

            // 5. Write CSV
            writeCsvReport(reportDir, anomalies);

            return new Result(reportDir.toAbsolutePath().toString(), anomalies.size());
        } catch (IOException e) {
            throw new EodReconciliationException("Failed to export EOD report for date " + date, e);        }
    }

    private List<Map<String, Object>> fetchTrialBalance() {
        return jdbc.queryForList(TRIAL_BALANCE_SQL);
    }

    private List<Map<String, Object>> fetchUnsettled(LocalDate date) {
        return jdbc.queryForList(UNSETTLED_SQL, date);
    }

    private List<Map<String, Object>> detectAnomalies(List<Map<String, Object>> trialBalance,
                                                      List<Map<String, Object>> unsettled) {
        List<Map<String, Object>> anomalies = new ArrayList<>();

        for (Map<String, Object> row : trialBalance) {
            BigDecimal total = (BigDecimal) row.get(KEY_TOTAL);
            if (total.compareTo(BigDecimal.ZERO) != 0) {
                anomalies.add(Map.of(
                        KEY_TYPE, TYPE_TRIAL_BALANCE_MISMATCH,
                        KEY_CURRENCY, row.get(KEY_CURRENCY),
                        KEY_TOTAL, total
                ));
            }
        }

        for (Map<String, Object> row : unsettled) {
            anomalies.add(Map.of(
                    KEY_TYPE, TYPE_UNSETTLED_PAST_DUE,
                    KEY_SETTLEMENT_ID, row.get("id"),
                    KEY_TRADE_ID, row.get("trade_id"),
                    KEY_VALUE_DATE, row.get("value_date"),
                    KEY_STATUS, row.get(KEY_STATUS)
            ));
        }
        return anomalies;
    }

    private Path prepareOutputDir(LocalDate date) throws IOException {
        Path dir = reportsBaseDir.resolve(date.toString());
        Files.createDirectories(dir);
        return dir;
    }

    private void writeJsonReport(Path dir, List<Map<String, Object>> anomalies) throws IOException {
        Path jsonFile = dir.resolve(JSON_FILE_NAME);
        mapper.writerWithDefaultPrettyPrinter().writeValue(jsonFile.toFile(), anomalies);
    }

    private void writeCsvReport(Path dir, List<Map<String, Object>> anomalies) throws IOException {
        Path csvFile = dir.resolve(CSV_FILE_NAME);

        try (CSVPrinter printer = new CSVPrinter(
                new OutputStreamWriter(Files.newOutputStream(csvFile)),
                CSV_FORMAT)) {
            for (Map<String, Object> a : anomalies) {
                printer.printRecord(
                        a.getOrDefault(KEY_TYPE, ""),
                        a.getOrDefault(KEY_CURRENCY, ""),
                        a.getOrDefault(KEY_TOTAL, ""),
                        a.getOrDefault(KEY_SETTLEMENT_ID, ""),
                        a.getOrDefault(KEY_TRADE_ID, ""),
                        a.getOrDefault(KEY_VALUE_DATE, ""),
                        a.getOrDefault(KEY_STATUS, "")
                );
            }
        }
    }
}
