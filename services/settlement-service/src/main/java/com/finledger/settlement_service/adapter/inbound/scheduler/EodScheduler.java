package com.finledger.settlement_service.adapter.inbound.scheduler;

import com.finledger.settlement_service.application.port.inbound.RunEodReconciliationUseCase;
import java.time.LocalDate;
import java.time.ZoneId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;

public class EodScheduler {
  private static final Logger log = LoggerFactory.getLogger(EodScheduler.class);

  private final RunEodReconciliationUseCase runEod;

  public EodScheduler(RunEodReconciliationUseCase runEod) {
    this.runEod = runEod;
  }

  /** Runs every day at 23:59 UTC. Cron format: second minute hour day month day-of-week */
  @Scheduled(cron = "0 59 23 * * *", zone = "UTC")
  public void runEodJob() {
    LocalDate date = LocalDate.now(ZoneId.of("UTC"));
    var result = runEod.execute(date);
    log.info(
        "EOD reconciliation completed for {}. Report: {}, anomalies: {}",
        date,
        result.reportPath(),
        result.anomalies());
  }
}
