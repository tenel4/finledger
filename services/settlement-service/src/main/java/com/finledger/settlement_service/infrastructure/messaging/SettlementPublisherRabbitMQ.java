package com.finledger.settlement_service.infrastructure.messaging;

import com.finledger.settlement_service.config.RabbitTopologyConfig;
import com.finledger.settlement_service.domain.model.Settlement;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SettlementPublisherRabbitMQ {
    private static final Logger log = LoggerFactory.getLogger(SettlementPublisherRabbitMQ.class);
    private final RabbitTemplate rabbitTemplate;

    public void publish(Settlement settlement, String messageId , String correlationId) {
        rabbitTemplate.convertAndSend(RabbitTopologyConfig.EXCHANGE_SETTLEMENT, "", settlement, message -> {
            message.getMessageProperties().setContentType("application/json");
            message.getMessageProperties().setMessageId(messageId);
            message.getMessageProperties().setHeader("correlationId", correlationId);
            message.getMessageProperties().setHeader("eventType", "TradeCreated");
            return message;
        });
        log.info("Published SettlementCreated event: settlementId={} messageId={} correlationId={}", settlement.id().value(), messageId, correlationId);

    }
}
