package com.finledger.settlement_service.repository;


import com.finledger.settlement_service.model.Settlement;
import com.finledger.settlement_service.repository.base.AbstractRepositoryTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static com.finledger.settlement_service.model.enums.SettlementStatus.*;
import static org.assertj.core.api.Assertions.assertThat;

class SettlementRepositoryTest extends AbstractRepositoryTest {
    @Autowired
    private SettlementRepository settlementRepository;

    @Test
    void testSaveAndFindSettlement() {
        Settlement settlement = new Settlement();
        settlement.setTradeId(UUID.randomUUID());
        settlement.setValueDate(LocalDate.now());
        settlement.setGrossAmount(BigDecimal.valueOf(1000.00));
        settlement.setFees(BigDecimal.valueOf(10.00));
        settlement.setNetAmount(BigDecimal.valueOf(990.00));
        settlement.setStatus(PENDING);
        settlement.setMessageKey(UUID.randomUUID());

        Settlement saved = settlementRepository.save(settlement);
        assertThat(saved.getId()).isNotNull();
    }
}
