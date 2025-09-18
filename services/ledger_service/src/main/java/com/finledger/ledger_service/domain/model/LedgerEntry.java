package com.finledger.ledger_service.domain.model;

import com.finledger.ledger_service.domain.value.SignedMoney;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class LedgerEntry {
    private final UUID id;
    private final Instant entryTime;
    private final UUID accountId;
    private final SignedMoney amount;
    private final LedgerEntryReferenceType referenceType;
    private final UUID referenceId;
    private final UUID messageId;

    private LedgerEntry(UUID id, Instant entryTime, UUID accountId, SignedMoney amount,
                        LedgerEntryReferenceType referenceType, UUID referenceId, UUID messageId) {
        validate(id, entryTime, accountId, amount, referenceType, referenceId, messageId);
        this.id = id;
        this.entryTime = entryTime;
        this.accountId = accountId;
        this.amount = amount;
        this.referenceType = referenceType;
        this.referenceId = referenceId;
        this.messageId = messageId;
    }

    public static LedgerEntry createNew(UUID accountId, SignedMoney amount,
                                        LedgerEntryReferenceType referenceType,
                                        UUID referenceId, UUID messageId) {
        return new LedgerEntry(UUID.randomUUID(), Instant.now(), accountId, amount,
                referenceType, referenceId, messageId);
    }

    public static LedgerEntry rehydrate(UUID id, Instant entryTime, UUID accountId, SignedMoney amount,
                                        LedgerEntryReferenceType referenceType,
                                        UUID referenceId, UUID messageId) {
        return new LedgerEntry(id, entryTime, accountId, amount,
                referenceType, referenceId, messageId);
    }

    public boolean isDebit() { return amount.isDebit(); }
    public boolean isCredit() { return amount.isCredit(); }

    public UUID id() { return id; }
    public Instant entryTime() { return entryTime; }
    public UUID accountId() { return accountId; }
    public SignedMoney amount() { return amount; }
    public LedgerEntryReferenceType referenceType() { return referenceType; }
    public UUID referenceId() { return referenceId; }
    public UUID messageId() { return messageId; }

    private static void validate(UUID id, Instant entryTime, UUID accountId, SignedMoney amount,
                                 LedgerEntryReferenceType referenceType,
                                 UUID referenceId, UUID messageId) {
        Objects.requireNonNull(id, "LedgerEntry: ID cannot be null");
        Objects.requireNonNull(entryTime, "LedgerEntry: Entry Time cannot be null");
        Objects.requireNonNull(accountId, "LedgerEntry: Account ID cannot be null");
        Objects.requireNonNull(amount, "LedgerEntry: Amount cannot be null");
        Objects.requireNonNull(referenceType, "LedgerEntry: Reference Type cannot be null");
        Objects.requireNonNull(referenceId, "LedgerEntry: Reference ID cannot be null");
        Objects.requireNonNull(messageId, "LedgerEntry: Message ID cannot be null");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LedgerEntry that)) return false;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
