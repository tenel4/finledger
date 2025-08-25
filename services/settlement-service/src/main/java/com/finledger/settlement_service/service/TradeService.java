package com.finledger.settlement_service.service;


import com.finledger.settlement_service.messaging.TradeCreatedEventPublisher;
import com.finledger.settlement_service.model.Trade;
import com.finledger.settlement_service.model.dto.CreateTradeRequest;
import com.finledger.settlement_service.model.dto.CreateTradeResponse;
import com.finledger.settlement_service.model.event.TradeCreatedEvent;
import com.finledger.settlement_service.repository.TradeRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;


@Service
@RequiredArgsConstructor
public class TradeService {

    private final TradeRepository tradeRepository;
    private final TradeCreatedEventPublisher tradeCreatedEventPublisher;

    @Transactional
    public CreateTradeResponse createTrade(CreateTradeRequest request) {
        Trade trade = new Trade();
        trade.setSymbol(request.symbol());
        trade.setSide(request.side());
        trade.setQuantity(request.quantity());
        trade.setPrice(request.price());
        trade.setCurrency(request.currency());
        trade.setBuyerAccountId(request.buyerAccountId());
        trade.setSellerAccountId(request.sellerAccountId());

        Trade saved = tradeRepository.save(trade);

        UUID messageKey = UUID.randomUUID();

        TradeCreatedEvent event = new TradeCreatedEvent(
                messageKey,
                saved.getId(),
                saved.getSymbol(),
                saved.getSide(),
                saved.getQuantity(),
                saved.getPrice(),
                saved.getCurrency(),
                saved.getBuyerAccountId(),
                saved.getSellerAccountId(),
                saved.getTradeTime()
        );

        tradeCreatedEventPublisher.publishTradeCreatedEvent(event);

        return new CreateTradeResponse(saved.getId(), saved.getTradeTime(), messageKey);
    }

}
