package com.finledger.settlement_service.infrastructure.messaging;

import com.finledger.settlement_service.domain.model.Settlement;
import com.finledger.settlement_service.domain.model.Trade;
import com.finledger.settlement_service.domain.port.ProcessedMessageRepository;
import com.finledger.settlement_service.domain.port.SettlementRepository;
import com.finledger.settlement_service.infrastructure.persistance.entity.ProcessedMessageEntity;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.util.UUID;

@Component
public class TradeConsumerRabbitMQ {
    private static final Logger log = LoggerFactory.getLogger(TradeConsumerRabbitMQ.class);

    private final SettlementRepository settlementRepository;
    private final ProcessedMessageRepository processedMessageRepository;
    private final SettlementPublisherRabbitMQ settlementPublisherRabbitMQ;

    public TradeConsumerRabbitMQ(SettlementRepository settlementRepository, ProcessedMessageRepository processedMessageRepository, SettlementPublisherRabbitMQ settlementPublisherRabbitMQ) {
        this.settlementRepository = settlementRepository;
        this.processedMessageRepository = processedMessageRepository;
        this.settlementPublisherRabbitMQ = settlementPublisherRabbitMQ;
    }

    @RabbitListener(queues = "${rabbitmq.queue.trade-created}")
    @Transactional
    public void consume(Trade trade,
                                   @Header(AmqpHeaders.MESSAGE_ID) String messageId,
                                   @Header(name = "correlationId", required = false) String correlationId) {
        UUID messageIdUUID = UUID.fromString(messageId);

        if (correlationId != null) {
            MDC.put("X-Correlation-Id", correlationId);
        }

        try {
            if (processedMessageRepository.existsById(messageIdUUID)) {
                log.info("Duplicate message detected, skipping processing: messageId={} correlationId={}", messageId, correlationId);
                return;
            }

            Settlement settlement = Settlement.createFromTrade(trade.id(), trade.notional(), trade.createdAt().atZone(ZoneId.systemDefault()).toLocalDate(), messageIdUUID);
            Settlement savedSettlement = settlementRepository.save(settlement);
            log.info("Settlement persisted: settlementId={} tradeId={} messageId={} correlationId={}", savedSettlement.id().value(), trade.id().value(), messageId, correlationId);
            processedMessageRepository.save(new ProcessedMessageEntity(messageIdUUID));

            String settlementMessageId = UUID.randomUUID().toString();
            settlementPublisherRabbitMQ.publish(settlement, settlementMessageId, correlationId);
            log.info("SettlementPublished event sent: settlementId={} messageId={} correlationId={}", savedSettlement.id().value(), settlementMessageId, correlationId);
        } catch (Exception e) {
            log.error("Error processing TradeCreated message: messageId={} correlationId={}", messageId, correlationId, e);
            throw e; // Rethrow to trigger message re-delivery
        } finally {
            MDC.remove("X-Correlation-Id");
        }
    }
}
