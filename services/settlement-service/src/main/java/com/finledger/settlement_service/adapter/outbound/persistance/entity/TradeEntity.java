package com.finledger.settlement_service.adapter.outbound.persistance.entity;

import com.finledger.settlement_service.domain.model.Trade;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "trade")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TradeEntity {
  @Id private UUID id;

  @Column(nullable = false, length = 12)
  private String symbol;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private Trade.Side side;

  @Column(nullable = false)
  private long quantity;

  @Column(nullable = false, precision = 18, scale = 4)
  private BigDecimal price;

  @Column(nullable = false, length = 3)
  private String currency;

  @Column(name = "buyer_account_id", nullable = false)
  private UUID buyerAccountId;

  @Column(name = "seller_account_id", nullable = false)
  private UUID sellerAccountId;

  @Column(name = "trade_time", nullable = false)
  private Instant tradeTime;
}
