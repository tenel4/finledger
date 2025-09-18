package com.finledger.settlement_service.domain.model;

import com.finledger.settlement_service.domain.value.Money;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

public final class Settlement {
    public enum Status { PENDING, SETTLED, FAILED }
    private static final BigDecimal DEFAULT_FEE_RATE = BigDecimal.valueOf(0.0003);

    private final UUID id;
    private final UUID tradeId;
    private final LocalDate valueDate;
    private final Money grossAmount;
    private final Money fees;
    private final Money netAmount;
    private final Status status;
    private final UUID messageId;

    private Settlement(UUID id, UUID tradeId, LocalDate valueDate, Money grossAmount,
                       Money fees, Money netAmount, Status status, UUID messageId) {
        validate(id, tradeId, valueDate, grossAmount, fees, netAmount, status, messageId);
        this.id = id;
        this.tradeId = tradeId;
        this.valueDate = valueDate;
        this.grossAmount = grossAmount;
        this.fees = fees;
        this.netAmount = netAmount;
        this.status = status;
        this.messageId = messageId;
    }

    public static Settlement createNew(UUID tradeId, LocalDate valueDate, Money grossAmount,
                                       Status status, UUID messageId) {
        Money fees = grossAmount.times(DEFAULT_FEE_RATE); // TODO hard coded
        Money netAmount = grossAmount.subtract(fees);
        return new Settlement(UUID.randomUUID(), tradeId, valueDate, grossAmount, fees, netAmount, status, messageId);
    }

    public static Settlement rehydrate(UUID id, UUID tradeId, LocalDate valueDate, Money grossAmount,
                                       Money fees, Money netAmount, Status status, UUID messageId) {
        Objects.requireNonNull(grossAmount, "Settlement: Gross Amount cannot be null");
        Objects.requireNonNull(netAmount, "Settlement: Net Amount cannot be null");
        Objects.requireNonNull(fees, "Settlement: Fees cannot be null");
        if (!grossAmount.subtract(fees).equals(netAmount)) {
            throw new IllegalArgumentException("Settlement: netAmount must equal grossAmount - fees");
        }
        return new Settlement(id, tradeId, valueDate, grossAmount, fees, netAmount, status, messageId);
    }

    public boolean isPending() { return status == Status.PENDING; }
    public boolean isSettled() { return status == Status.SETTLED; }
    public boolean isFailed() { return status == Status.FAILED; }

    public UUID id() { return id; }
    public UUID tradeId() { return tradeId; }
    public LocalDate valueDate() { return valueDate; }
    public Money grossAmount() { return grossAmount; }
    public Money fees() { return fees; }
    public Money netAmount() { return netAmount; }
    public Status status() { return status; }
    public UUID messageId() { return messageId; }

    private static void validate(UUID id, UUID tradeId, LocalDate valueDate, Money grossAmount,
                                 Money fees, Money netAmount, Status status, UUID messageId) {
        Objects.requireNonNull(id, "Settlement: ID cannot be null");
        Objects.requireNonNull(tradeId, "Settlement: Trade ID cannot be null");
        Objects.requireNonNull(valueDate, "Settlement: Value Date cannot be null");
        Objects.requireNonNull(grossAmount, "Settlement: Gross Amount cannot be null");
        Objects.requireNonNull(fees, "Settlement: Fees cannot be null");
        Objects.requireNonNull(netAmount, "Settlement: Net Amount cannot be null");
        Objects.requireNonNull(status, "Settlement: Status cannot be null");
        Objects.requireNonNull(messageId, "Settlement: Message ID cannot be null");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Settlement that)) return false;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
