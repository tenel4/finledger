package com.finledger.ledger_service.config;

import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.retry.RejectAndDontRequeueRecoverer;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.interceptor.RetryOperationsInterceptor;

@Configuration
public class CommonRabbitConfig {

  @Bean
  public Jackson2JsonMessageConverter messageConverter() {
    return new Jackson2JsonMessageConverter();
  }

  @Bean
  public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
    RabbitTemplate template = new RabbitTemplate(connectionFactory);
    template.setMessageConverter(messageConverter());

    template.setBeforePublishPostProcessors(
        message -> {
          String correlationId = MDC.get(CorrelationIdFilter.MDC_KEY);
          if (correlationId != null) {
            message
                .getMessageProperties()
                .setHeader(CorrelationIdFilter.CORRELATION_ID_HEADER, correlationId);
          }
          if (message.getMessageProperties().getMessageId() == null) {
            message.getMessageProperties().setMessageId(UUID.randomUUID().toString());
          }
          return message;
        });

    return template;
  }

  @Bean
  public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory cf) {
    RetryOperationsInterceptor retryInterceptor =
        RetryInterceptorBuilder.stateless()
            .maxAttempts(3)
            .backOffOptions(1000, 2.0, 10000)
            .recoverer(new RejectAndDontRequeueRecoverer())
            .build();

    SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
    factory.setConnectionFactory(cf);
    factory.setMessageConverter(messageConverter());
    factory.setAdviceChain(retryInterceptor);
    return factory;
  }
}
