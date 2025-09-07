package com.finledger.settlement_service.infrastructure.messaging;

import com.finledger.settlement_service.config.RabbitTopologyConfig;
import com.finledger.settlement_service.domain.model.Trade;
import com.finledger.settlement_service.domain.port.TradePublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class TradePublisherRabbitMQ implements TradePublisher {
    private static final Logger log = LoggerFactory.getLogger(TradePublisherRabbitMQ.class);

    private final RabbitTemplate template;

    public TradePublisherRabbitMQ(RabbitTemplate template) {
        this.template = template;
    }

    @Override
    public void publishTradeCreated(Trade trade, String messageId, String correlationId) {
        template.convertAndSend(RabbitTopologyConfig.EXCHANGE_TRADE, "", trade, message -> {
            message.getMessageProperties().setContentType("application/json");
            message.getMessageProperties().setMessageId(messageId);
            message.getMessageProperties().setHeader("correlationId", correlationId);
            message.getMessageProperties().setHeader("eventType", "TradeCreated");
            return message;
        });
        log.info("Published TradeCreated event: tradeId={} messageId={} correlationId={}", trade.id().value(), messageId, correlationId);
    }
}
