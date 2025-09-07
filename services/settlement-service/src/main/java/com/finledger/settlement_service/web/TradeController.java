package com.finledger.settlement_service.web;

import com.finledger.settlement_service.application.TradeApplicationService;
import com.finledger.settlement_service.application.dto.CreateTradeRequest;
import com.finledger.settlement_service.application.dto.CreateTradeResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/trades")
public class TradeController {
    private final TradeApplicationService tradeApplicationService;

    public TradeController(TradeApplicationService tradeApplicationService) {
        this.tradeApplicationService = tradeApplicationService;
    }

    @PostMapping
    public ResponseEntity<CreateTradeResponse> createTrade(@RequestBody @Valid CreateTradeRequest request) {
        CreateTradeResponse response = tradeApplicationService.createTrade(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
