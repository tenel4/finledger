package com.finledger.settlement_service.repository;

import com.finledger.settlement_service.model.ProcessedMessage;
import com.finledger.settlement_service.repository.base.AbstractRepositoryTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class ProcessedMessageRepositoryTest extends AbstractRepositoryTest {
    @Autowired
    private ProcessedMessageRepository processedMessageRepository;

    @Test
    void testSaveAndFindProcessedMessage() {
        ProcessedMessage pmr = new ProcessedMessage();
        pmr.setMessageKey(java.util.UUID.randomUUID());
        pmr.setProcessedAt(Instant.now());

        ProcessedMessage saved = processedMessageRepository.save(pmr);
        assertThat(saved.getMessageKey()).isNotNull();
    }
}
