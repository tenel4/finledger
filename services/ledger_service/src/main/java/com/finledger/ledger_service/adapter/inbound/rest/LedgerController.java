package com.finledger.ledger_service.adapter.inbound.rest;

import com.finledger.ledger_service.application.port.inbound.GetLedgerSummaryUseCase;
import com.finledger.ledger_service.application.port.inbound.GetLedgerEntriesUseCase;
import com.finledger.ledger_service.domain.model.LedgerEntry;
import com.finledger.ledger_service.util.DateConversionUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/ledger")
public class LedgerController {

    private final GetLedgerEntriesUseCase getLedgerEntries;
    private final GetLedgerSummaryUseCase getSummary;
    private final ZoneId zoneId = ZoneId.of("UTC");

    public LedgerController(GetLedgerEntriesUseCase getLedgerEntries, GetLedgerSummaryUseCase getSummary) {
        this.getLedgerEntries = getLedgerEntries;
        this.getSummary = getSummary;
    }

    @GetMapping
    public ResponseEntity<List<LedgerEntryResponse>> getLedgerEntries(
            @RequestParam UUID accountId,
            @RequestParam LocalDate from,
            @RequestParam LocalDate to) {

        Instant fromInstant = DateConversionUtil.startOfDay(from, zoneId);
        Instant toInstant = DateConversionUtil.startOfNextDay(to, zoneId);

        List<LedgerEntry> entries = getLedgerEntries.execute(accountId, fromInstant, toInstant);

        return ResponseEntity.ok(entries.stream()
                .map(e -> new LedgerEntryResponse(
                        e.id(), e.entryTime(), e.accountId(), e.amount().currency().getCurrencyCode(),
                        e.amount().amount(), e.referenceType(), e.referenceId()))
                .toList());
    }

    @GetMapping("/summary")
    public ResponseEntity<List<LedgerSummaryResponse>> getLedgerSummary(
            @RequestParam LocalDate date) {

        Instant dateInstant = DateConversionUtil.startOfDay(date, zoneId);

        List<GetLedgerSummaryUseCase.Result> summaries = getSummary.execute(dateInstant);

        return ResponseEntity.ok(summaries.stream()
                .map(s -> new LedgerSummaryResponse(s.accountId(), s.currency(), s.sum()))
                .toList());
    }
    }
