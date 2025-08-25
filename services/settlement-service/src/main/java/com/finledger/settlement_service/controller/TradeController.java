package com.finledger.settlement_service.controller;

import com.finledger.settlement_service.model.dto.CreateTradeRequest;
import com.finledger.settlement_service.model.dto.CreateTradeResponse;
import com.finledger.settlement_service.service.TradeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/trades")
@RequiredArgsConstructor
public class TradeController {

    private final TradeService tradeService;

    @PostMapping
    public ResponseEntity<CreateTradeResponse> createTrade(@Valid @RequestBody CreateTradeRequest request) {
        CreateTradeResponse response = tradeService.createTrade(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
