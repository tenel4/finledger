package com.finledger.settlement_service.config;

import org.springframework.amqp.core.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TradeRabbitConfig {

    private final RabbitMQBindingsProperties props;

    public TradeRabbitConfig(RabbitMQBindingsProperties props) {
        this.props = props;
    }

    // ----- Trade -----
    @Bean
    @ConditionalOnProperty(name = "messaging.trade.declare", havingValue = "true", matchIfMissing = true)
    DirectExchange tradeExchange() {
        return new DirectExchange(props.getTrade().getExchange());
    }

    @Bean
    @ConditionalOnProperty(name = "messaging.trade.declare", havingValue = "true", matchIfMissing = true)
    DirectExchange tradeRetryExchange() {
        return new DirectExchange(props.getTrade().getRetryExchange());
    }

    @Bean
    @ConditionalOnProperty(name = "messaging.trade.declare", havingValue = "true", matchIfMissing = true)
    Queue tradeQueue() {
        return QueueBuilder.durable(props.getTrade().getQueue())
                .withArgument("x-dead-letter-exchange", props.getTrade().getRetryExchange())
                .withArgument("x-dead-letter-routing-key", props.getTrade().getRetryRoutingKey())
                .build();
    }

    @Bean
    @ConditionalOnProperty(name = "messaging.trade.declare", havingValue = "true", matchIfMissing = true)
    Queue tradeRetryQueue() {
        return QueueBuilder.durable(props.getTrade().getRetryQueue())
                .withArgument("x-dead-letter-exchange", props.getTrade().getExchange())
                .withArgument("x-dead-letter-routing-key", props.getTrade().getRoutingKey())
                .withArgument("x-message-ttl", 5000)
                .build();
    }

    @Bean
    @ConditionalOnProperty(name = "messaging.trade.declare", havingValue = "true", matchIfMissing = true)
    Queue tradeDlq() {
        return QueueBuilder.durable(props.getTrade().getDlq()).build();
    }

    @Bean
    @ConditionalOnProperty(name = "messaging.trade.declare", havingValue = "true", matchIfMissing = true)
    Binding bindingTradeMain() {
        return BindingBuilder.bind(tradeQueue())
                .to(tradeExchange())
                .with(props.getTrade().getRoutingKey());
    }

    @Bean
    @ConditionalOnProperty(name = "messaging.trade.declare", havingValue = "true", matchIfMissing = true)
    Binding bindingTradeRetry() {
        return BindingBuilder.bind(tradeRetryQueue())
                .to(tradeRetryExchange())
                .with(props.getTrade().getRetryRoutingKey());
    }

    @Bean
    @ConditionalOnProperty(name = "messaging.trade.declare", havingValue = "true", matchIfMissing = true)
    Binding bindingTradeDlq() {
        return BindingBuilder.bind(tradeDlq())
                .to(tradeExchange())
                .with(props.getTrade().getDlq());
    }

    // ----- Settlement publishing-only (no consumer here) -----
    @Bean
    @ConditionalOnProperty(name = "messaging.settlement.declareExchange", havingValue = "true", matchIfMissing = true)
    DirectExchange settlementExchange() {
        return new DirectExchange(props.getSettlement().getExchange());
    }
}
