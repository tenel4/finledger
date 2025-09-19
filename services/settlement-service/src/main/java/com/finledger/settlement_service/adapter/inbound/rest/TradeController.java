package com.finledger.settlement_service.adapter.inbound.rest;

import com.finledger.settlement_service.adapter.inbound.rest.dto.CreateTradeRequest;
import com.finledger.settlement_service.adapter.inbound.rest.dto.CreateTradeResponse;
import com.finledger.settlement_service.adapter.inbound.rest.dto.GetSettlementsResponse;
import com.finledger.settlement_service.adapter.inbound.rest.dto.GetTradesResponse;
import com.finledger.settlement_service.adapter.inbound.rest.mapper.SettlementMapper;
import com.finledger.settlement_service.adapter.inbound.rest.mapper.TradeMapper;
import com.finledger.settlement_service.application.port.inbound.CreateTradeUseCase;
import com.finledger.settlement_service.application.port.inbound.GetSettlementsUseCase;
import com.finledger.settlement_service.application.port.inbound.GetTradesUseCase;
import com.finledger.settlement_service.application.port.inbound.RunEodReconciliationUseCase;
import com.finledger.settlement_service.domain.model.Settlement;
import com.finledger.settlement_service.domain.model.Trade;
import com.finledger.settlement_service.application.port.outbound.EodReconciliationPort;
import com.finledger.settlement_service.util.DateConversionUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@RestController
@RequestMapping("/api")
public class TradeController {
    private final CreateTradeUseCase createTrade;
    private final GetTradesUseCase getTrades;
    private final GetSettlementsUseCase getSettlements;
    private final RunEodReconciliationUseCase runEod;

    @Value("${app.timezone:Europe/London}")
    private ZoneId defaultZoneId;

    public TradeController(CreateTradeUseCase createTrade,
                           GetTradesUseCase getTrades,
                           GetSettlementsUseCase getSettlements,
                           RunEodReconciliationUseCase runEod) {
        this.createTrade = createTrade;
        this.getTrades = getTrades;
        this.getSettlements = getSettlements;
        this.runEod = runEod;
    }

    @PostMapping("/trades")
    public ResponseEntity<CreateTradeResponse> createTrade(@RequestBody @Valid CreateTradeRequest req) {
        var command = new CreateTradeUseCase.Command(
                req.symbol(), req.side(), req.quantity(), req.price(),
                req.currency(), req.buyerAccountId(), req.sellerAccountId());

        var result = createTrade.execute(command);
        var response = new CreateTradeResponse(result.id(), result.tradeTime(), result.messageId());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/trades")
    public ResponseEntity<List<GetTradesResponse>> getTrades(
            @RequestParam String symbol,
            @RequestParam LocalDate from,
            @RequestParam LocalDate to,
            @RequestParam Trade.Side side // TODO decouple domain enum and rest
    ) {
        var fromI = DateConversionUtil.startOfDay(from, defaultZoneId);
        var toI = DateConversionUtil.startOfNextDay(to, defaultZoneId);

        List<Trade> trades = getTrades.execute(symbol, fromI, toI, side); // TODO decouple domain model and rest
        var result = trades.stream()
                .map(TradeMapper::toResponse)
                .toList();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/settlements")
    public ResponseEntity<List<GetSettlementsResponse>> listSettlements(
            @RequestParam Settlement.Status status, // TODO decouple domain enum and rest
            @RequestParam LocalDate date
    ) {
        List<Settlement> settlements = getSettlements.execute(status, date); // TODO decouple domain model and rest
        var result = settlements.stream()
                .map(SettlementMapper::toResponse)
                .toList();
        return ResponseEntity.ok(result);
    }

    @PostMapping("/eod/run")
    public ResponseEntity<EodReconciliationPort.Result> runEod(@RequestParam LocalDate date) {
        var res = runEod.execute(date);
        return ResponseEntity.accepted().body(res);
    }
}
