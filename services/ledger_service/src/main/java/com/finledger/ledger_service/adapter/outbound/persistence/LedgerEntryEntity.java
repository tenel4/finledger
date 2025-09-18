package com.finledger.ledger_service.adapter.outbound.persistence;

import com.finledger.ledger_service.domain.model.LedgerEntryReferenceType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "ledger_entry")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LedgerEntryEntity {
    @Id
    private UUID id;
    @Column(name = "entry_time", nullable = false)
    private Instant entryTime;
    @Column(name = "account_id", nullable = false)
    private UUID accountId;
    @Column(name = "currency", nullable = false, length = 3)
    private String currency;
    @Column(name = "amount_signed", nullable = false, precision = 18, scale = 4)
    private BigDecimal amountSigned;
    @Column(name = "reference_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private LedgerEntryReferenceType referenceType;
    @Column(name = "reference_id", nullable = false)
    private UUID referenceId;
    @Column(name = "message_key", nullable = false)
    private UUID messageId;

}
