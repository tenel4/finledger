package com.finledger.settlement_service.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String TRADE_EXCHANGE = "trade.exchange";
    public static final String TRADE_QUEUE = "trade.queue";
    public static final String TRADE_ROUTING_KEY = "trade.created";

    @Bean
    Queue tradeQueue() {
        return new Queue(TRADE_QUEUE, true);
    }

    @Bean
    DirectExchange tradeExchange() {
        return new DirectExchange(TRADE_EXCHANGE);
    }

    @Bean
    Binding tradeBinding(Queue tradeQueue, DirectExchange tradeExchange) {
        return BindingBuilder.bind(tradeQueue).to(tradeExchange).with(TRADE_ROUTING_KEY);
    }

    @Bean
    public Jackson2JsonMessageConverter producerJackson2MessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(final ConnectionFactory connectionFactory) {
        final RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(producerJackson2MessageConverter());
        return rabbitTemplate;
    }
}
