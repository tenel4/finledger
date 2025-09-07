package com.finledger.settlement_service.infrastructure.persistance.entity;

import com.finledger.settlement_service.domain.model.Side;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "trade")
@Getter
@Setter
public class TradeEntity {
    @Id
    @GeneratedValue
    private UUID id;
    @Column(nullable = false, length = 12)
    private String symbol;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Side side;
    @Column(nullable = false)
    private long quantity;
    @Column(nullable = false, precision = 18, scale = 4)
    private BigDecimal price;
    @Column(nullable = false, length = 3)
    private String currency;
    @Column(name = "notional_amount", nullable = false)
    private BigDecimal notionalAmount;
    @Column(name = "buyer_account_id", nullable = false)
    private UUID buyerAccountId;
    @Column(name = "seller_account_id", nullable = false)
    private UUID sellerAccountId;
    @Column(name = "trade_time", nullable = false)
    private Instant createdAt;

}
