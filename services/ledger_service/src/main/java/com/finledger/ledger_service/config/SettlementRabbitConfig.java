package com.finledger.ledger_service.config;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SettlementRabbitConfig {

    @Value("${messaging.settlement.exchange}")
    private String exchange;

    @Value("${messaging.settlement.queue}")
    private String queue;

    @Value("${messaging.settlement.retryQueue}")
    private String retryQueue;

    @Value("${messaging.settlement.dlq}")
    private String dlq;

    @Value("${messaging.settlement.routingKey}")
    private String routingKey;

    @Bean
    public DirectExchange settlementExchange() {
        return new DirectExchange(exchange, true, false);
    }

    @Bean
    public Queue settlementQueue() {
        return QueueBuilder.durable(queue)
                .withArgument("x-dead-letter-exchange", exchange)
                .withArgument("x-dead-letter-routing-key", retryQueue)
                .build();
    }

    @Bean
    public Queue settlementRetryQueue() {
        return QueueBuilder.durable(retryQueue)
                .withArgument("x-dead-letter-exchange", exchange)
                .withArgument("x-dead-letter-routing-key", routingKey)
                .withArgument("x-message-ttl", 5000)
                .build();
    }

    @Bean
    public Queue settlementDlq() {
        return QueueBuilder.durable(dlq).build();
    }

    @Bean
    public Binding bindingSettlementCreated() {
        return BindingBuilder.bind(settlementQueue()).to(settlementExchange()).with(routingKey);
    }
}
