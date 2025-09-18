package com.finledger.ledger_service.application.port.outbound;


import java.util.UUID;

public interface ProcessedMessageRepositoryPort {
    boolean markProcessedIfNew(UUID messageKey);
}
