package com.finledger.settlement_service.messaging;

import com.finledger.settlement_service.model.event.TradeCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import static com.finledger.settlement_service.config.RabbitConfig.*;

@Component
@RequiredArgsConstructor
public class TradeCreatedEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishTradeCreatedEvent(TradeCreatedEvent event) {
        rabbitTemplate.convertAndSend(TRADE_EXCHANGE, TRADE_ROUTING_KEY, event);
    }
}
