package com.finledger.settlement_service.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitTopologyConfig {
    // --- TRADE ---
    public static final String EXCHANGE_TRADE = "trade.exchange";
    public static final String QUEUE_TRADE_CREATED = "trade.created.queue";
    public static final String QUEUE_TRADE_CREATED_RETRY = "trade.created.retry";
    public static final String QUEUE_TRADE_CREATED_DLQ = "trade.created.dlq";

    // --- SETTLEMENT ---
    public static final String EXCHANGE_SETTLEMENT = "settlement.exchange";
    public static final String QUEUE_SETTLEMENT_CREATED = "settlement.created.queue";
    public static final String QUEUE_SETTLEMENT_CREATED_RETRY = "settlement.created.retry";
    public static final String QUEUE_SETTLEMENT_CREATED_DLQ = "settlement.created.dlq";

    @Bean
    DirectExchange tradeCreatedExchange() {
        return new DirectExchange(EXCHANGE_TRADE, true, false);
    }

    @Bean
    Queue tradeCreatedQueue() {
        return QueueBuilder.durable(QUEUE_TRADE_CREATED)
                .withArgument("x-dead-letter-exchange", EXCHANGE_TRADE)
                .withArgument("x-dead-letter-routing-key", QUEUE_TRADE_CREATED_RETRY)
                .build();
    }

    @Bean
    Queue tradeCreatedRetryQueue() {
        return QueueBuilder.durable(QUEUE_TRADE_CREATED_RETRY)
                .withArgument("x-dead-letter-exchange", EXCHANGE_TRADE)
                .withArgument("x-dead-letter-routing-key", QUEUE_TRADE_CREATED_DLQ)
                .withArgument("x-message-ttl", 5000) // 5 seconds delay
                .build();
    }

    @Bean
    Queue tradeCreatedDLQ() {
        return QueueBuilder.durable(QUEUE_TRADE_CREATED_DLQ).build();
    }

    @Bean
    Binding bindingTradeCreated() {
        return BindingBuilder.bind(tradeCreatedQueue()).to(tradeCreatedExchange()).with("");
    }

    @Bean
    DirectExchange settlementCreatedExchange() {
        return new DirectExchange(EXCHANGE_SETTLEMENT, true, false);
    }

    @Bean
    Queue settlementCreatedQueue() {
        return QueueBuilder.durable(QUEUE_SETTLEMENT_CREATED)
                .withArgument("x-dead-letter-exchange", EXCHANGE_SETTLEMENT)
                .withArgument("x-dead-letter-routing-key", QUEUE_SETTLEMENT_CREATED_RETRY)
                .build();
    }

    @Bean
    Queue settlementCreatedRetryQueue() {
        return QueueBuilder.durable(QUEUE_SETTLEMENT_CREATED_RETRY)
                .withArgument("x-dead-letter-exchange", EXCHANGE_SETTLEMENT)
                .withArgument("x-dead-letter-routing-key", QUEUE_SETTLEMENT_CREATED_DLQ)
                .withArgument("x-message-ttl", 5000)
                .build();
    }

    @Bean
    Queue settlementCreatedDLQ() {
        return QueueBuilder.durable(QUEUE_SETTLEMENT_CREATED_DLQ).build();
    }

    @Bean
    Binding bindingSettlementCreated() {
        return BindingBuilder.bind(settlementCreatedQueue()).to(settlementCreatedExchange()).with("");
    }
}
