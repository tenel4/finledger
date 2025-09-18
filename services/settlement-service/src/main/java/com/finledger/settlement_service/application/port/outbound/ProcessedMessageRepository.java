package com.finledger.settlement_service.application.port.outbound;

import java.util.UUID;

public interface ProcessedMessageRepository {
    boolean markProcessedIfNew(UUID messageId);
}
