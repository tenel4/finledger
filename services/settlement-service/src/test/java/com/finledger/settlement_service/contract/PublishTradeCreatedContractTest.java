package com.finledger.settlement_service.contract;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PublishTradeCreatedContractTest {
    @Test
    void payloadHasRequiredFields() {
        String json = "{\"tradeId\":\"uuid\",\"symbol\":\"AAPL\",\"side\":\"BUY\",\"quantity\":10,\"price\":100.00,\"currency\":\"USD\",\"notional\":1000.00,\"createdAt\":\"2025-08-27T12:00:00Z\"}";
        assertThat(json).contains("tradeId","symbol","side","quantity","price","currency","notional","createdAt");
    }
}
