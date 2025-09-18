package com.finledger.settlement_service.adapter.outbound.persistance.entity;

import com.finledger.settlement_service.domain.model.Settlement;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "settlement")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SettlementEntity {
    @Id
    private UUID id;
    @Column(name = "trade_id", nullable = false)
    private UUID tradeId;
    @Column(name = "value_date", nullable = false)
    private LocalDate valueDate;
    @Column(name = "gross_amount", nullable = false)
    private BigDecimal grossAmount;
    @Column(name = "fees", nullable = false)
    private BigDecimal fees;
    @Column(name = "net_amount", nullable = false)
    private BigDecimal netAmount;
    @Column(name = "currency", nullable = false)
    private String currency;
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Settlement.Status status;
    @Column(name = "message_key", nullable = false, unique = true)
    private UUID messageId;

}
