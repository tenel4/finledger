package com.finledger.settlement_service.domain.model;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

public class Settlement {
    private static final BigDecimal FEE_RATE = BigDecimal.valueOf(0.0003); // 0.03%

    private final SettlementId id;
    private final TradeId tradeId;
    private final LocalDate valueDate;
    private final Money grossAmount;
    private final Money fees;
    private final Money netAmount;
    private final SettlementStatus status;
    private final UUID messageKey;

    private Settlement(TradeId tradeId, LocalDate valueDate, Money grossAmount, Money fees, SettlementStatus status, UUID messageKey) {
        Objects.requireNonNull(tradeId, "Settlement: Trade ID cannot be null");
        Objects.requireNonNull(valueDate, "Settlement: Value Date cannot be null");
        Objects.requireNonNull(grossAmount, "Settlement: Gross Amount cannot be null");
        Objects.requireNonNull(fees, "Settlement: Fees cannot be null");
        Objects.requireNonNull(status, "Settlement: Status cannot be null");
        Objects.requireNonNull(messageKey, "Settlement: Message Key cannot be null");

        this.id = SettlementId.newId();
        this.tradeId = tradeId;
        this.valueDate = valueDate;
        this.grossAmount = grossAmount;
        this.fees = fees;
        this.netAmount = grossAmount.minus(fees);
        this.status = status;
        this.messageKey = messageKey;
    }

    public static Settlement createFromTrade(TradeId tradeId, Money grossAmount, LocalDate tradeDate, UUID messageKey) {
        LocalDate valueDate = calculateValueDate(tradeDate); // T+2 settlement
        Money fees = calculateFees(grossAmount);
        return new Settlement(tradeId, valueDate, grossAmount, fees, SettlementStatus.PENDING, messageKey);
    }

    private static LocalDate calculateValueDate(LocalDate tradeDate) {
        LocalDate valueDate = tradeDate.plusDays(2);
        if (valueDate.getDayOfWeek() == DayOfWeek.SATURDAY) {
            valueDate = valueDate.plusDays(2);
        } else if (valueDate.getDayOfWeek() == DayOfWeek.SUNDAY) {
            valueDate = valueDate.plusDays(1);
        }
        return valueDate;
    }

    private static Money calculateFees(Money grossAmount) {
        return grossAmount.times(FEE_RATE);
    }

    public SettlementId id() { return id; }
    public TradeId tradeId() {return tradeId; }
    public LocalDate valueDate() { return valueDate; }
    public Money grossAmount() { return grossAmount; }
    public Money fees() { return fees; }
    public Money netAmount() { return netAmount; }
    public SettlementStatus status() { return status; }
    public UUID messageKey() { return messageKey; }
}
