package com.finledger.settlement_service.application.impl;

import com.finledger.settlement_service.application.TradeApplicationService;
import com.finledger.settlement_service.application.dto.CreateTradeRequest;
import com.finledger.settlement_service.application.dto.CreateTradeResponse;
import com.finledger.settlement_service.domain.model.Money;
import com.finledger.settlement_service.domain.model.Quantity;
import com.finledger.settlement_service.domain.model.Trade;
import com.finledger.settlement_service.domain.port.TradePublisher;
import com.finledger.settlement_service.domain.port.TradeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class TradeApplicationServiceImpl implements TradeApplicationService {
    private static final Logger log = LoggerFactory.getLogger(TradeApplicationServiceImpl.class);

    private final TradeRepository repository;
    private final TradePublisher publisher;

    public TradeApplicationServiceImpl(TradeRepository repository, TradePublisher publisher) {
        this.repository = repository;
        this.publisher = publisher;
    }

    @Override
    public CreateTradeResponse createTrade(CreateTradeRequest request) {
        Quantity quantity = Quantity.of(request.quantity());
        Money price = Money.of(request.price(), request.currency());

        Trade trade = Trade.of(
                request.symbol(),
                request.side(),
                quantity,
                price,
                request.buyerAccountId(),
                request.sellerAccountId(),
                Instant.now()
        );

        Trade savedTrade = repository.save(trade);
        log.info("Trade persisted: {}", savedTrade.id().value());

        String correlationId = Optional.ofNullable(MDC.get("correlationId")).orElse(UUID.randomUUID().toString());
        String messageId = UUID.randomUUID().toString();

        publisher.publishTradeCreated(trade, messageId, correlationId);
        log.info("TradeCreated published: tradeId={} correlationId={} messageId={}", trade.id().value(), correlationId, messageId);

        return new CreateTradeResponse(savedTrade.id().value(), messageId);
    }
}
