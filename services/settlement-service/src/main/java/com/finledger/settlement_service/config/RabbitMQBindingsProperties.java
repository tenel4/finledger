package com.finledger.settlement_service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "messaging")
public class RabbitMQBindingsProperties {

  private BindingProperties trade;
  private BindingProperties settlement;

  public BindingProperties getTrade() {
    return trade;
  }

  public void setTrade(BindingProperties trade) {
    this.trade = trade;
  }

  public BindingProperties getSettlement() {
    return settlement;
  }

  public void setSettlement(BindingProperties settlement) {
    this.settlement = settlement;
  }

  public static class BindingProperties {
    private boolean declare;
    private boolean declareExchange;
    private String exchange;
    private String routingKey;
    private String queue;
    private String retryExchange;
    private String retryQueue;
    private String retryRoutingKey;
    private String dlq;
    private String eventTypeHeader;

    public boolean isDeclare() {
      return declare;
    }

    public void setDeclare(boolean declare) {
      this.declare = declare;
    }

    public boolean isDeclareExchange() {
      return declareExchange;
    }

    public void setDeclareExchange(boolean declareExchange) {
      this.declareExchange = declareExchange;
    }

    public String getExchange() {
      return exchange;
    }

    public void setExchange(String exchange) {
      this.exchange = exchange;
    }

    public String getRoutingKey() {
      return routingKey;
    }

    public void setRoutingKey(String routingKey) {
      this.routingKey = routingKey;
    }

    public String getQueue() {
      return queue;
    }

    public void setQueue(String queue) {
      this.queue = queue;
    }

    public String getRetryExchange() {
      return retryExchange;
    }

    public void setRetryExchange(String retryExchange) {
      this.retryExchange = retryExchange;
    }

    public String getRetryQueue() {
      return retryQueue;
    }

    public void setRetryQueue(String retryQueue) {
      this.retryQueue = retryQueue;
    }

    public String getRetryRoutingKey() {
      return retryRoutingKey;
    }

    public void setRetryRoutingKey(String retryRoutingKey) {
      this.retryRoutingKey = retryRoutingKey;
    }

    public String getDlq() {
      return dlq;
    }

    public void setDlq(String dlq) {
      this.dlq = dlq;
    }

    public String getEventTypeHeader() {
      return eventTypeHeader;
    }

    public void setEventTypeHeader(String eventTypeHeader) {
      this.eventTypeHeader = eventTypeHeader;
    }
  }
}
