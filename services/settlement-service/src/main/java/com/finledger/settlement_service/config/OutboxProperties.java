package com.finledger.settlement_service.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "outbox.flush")
@Getter
@Setter
public class OutboxProperties {

    private boolean enabled = true;
    private int batchSize = 100;
    private int maxBatchesPerRun = 100;
    private int maxRetries = 8;

    private long backoffInitialMs = 1000;
    private double backoffMultiplier = 2.0;
    private long backoffMaxMs = 60000;
}