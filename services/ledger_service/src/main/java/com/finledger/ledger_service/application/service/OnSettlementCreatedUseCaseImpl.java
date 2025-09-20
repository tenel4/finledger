package com.finledger.ledger_service.application.service;

import com.finledger.ledger_service.application.port.inbound.OnSettlementCreatedUseCase;
import com.finledger.ledger_service.application.port.outbound.LedgerRepositoryPort;
import com.finledger.ledger_service.application.port.outbound.ProcessedMessageRepositoryPort;
import com.finledger.ledger_service.domain.model.LedgerEntry;
import com.finledger.ledger_service.domain.model.LedgerEntryReferenceType;
import com.finledger.ledger_service.domain.value.SignedMoney;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class OnSettlementCreatedUseCaseImpl implements OnSettlementCreatedUseCase {
    private final LedgerRepositoryPort ledgerRepo;
    private final ProcessedMessageRepositoryPort processedMessageRepository;

    public OnSettlementCreatedUseCaseImpl(LedgerRepositoryPort ledgerRepo,
                                          ProcessedMessageRepositoryPort processedMessageRepository) {
        this.ledgerRepo = ledgerRepo;
        this.processedMessageRepository = processedMessageRepository;
    }

    @Override
    @Transactional
    public void execute(UUID messageKey, UUID settlementId, UUID buyerAccountId, UUID sellerAccountId,
                        BigDecimal netAmount, String currency) {
        if (!processedMessageRepository.markProcessedIfNew(messageKey)) return;

        // Buyer pays out (negative amount → debit)
        LedgerEntry buyerDebit = LedgerEntry.createNew(
                buyerAccountId,
                SignedMoney.of(netAmount.negate(), currency),
                LedgerEntryReferenceType.SETTLEMENT,
                settlementId,
                messageKey
        );

        // Seller receives (positive amount → credit)
        LedgerEntry sellerCredit = LedgerEntry.createNew(
                sellerAccountId,
                SignedMoney.of(netAmount, currency),
                LedgerEntryReferenceType.SETTLEMENT,
                settlementId,
                messageKey
        );

        ledgerRepo.saveAll(List.of(buyerDebit, sellerCredit));
    }
}
