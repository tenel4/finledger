package com.finledger.settlement_service.model;

import com.finledger.settlement_service.model.enums.SettlementStatus;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "settlement")
@Data
public class Settlement {
    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "trade_id", nullable = false)
    private UUID tradeId;

    @Column(name = "value_date", nullable = false)
    private LocalDate valueDate;

    @Column(name = "gross_amount", nullable = false, precision = 18, scale = 4)
    private BigDecimal grossAmount;

    @Column(nullable = false, precision = 18, scale = 4)
    private BigDecimal fees;

    @Column(name = "net_amount", nullable = false, precision = 18, scale = 4)
    private BigDecimal netAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private SettlementStatus status;

    @Column(name = "message_key", nullable = false, unique = true)
    private UUID messageKey;
}
