package com.finledger.ledger_service.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(1)
public class CorrelationIdFilter implements Filter {
  public static final String CORRELATION_ID_HEADER = "X-Correlation-Id";
  public static final String MDC_KEY = "correlationId";

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    HttpServletRequest httpRequest = (HttpServletRequest) request;
    HttpServletResponse resp = (HttpServletResponse) response;

    String correlationId =
        Optional.ofNullable(httpRequest.getHeader(CORRELATION_ID_HEADER))
            .orElse(UUID.randomUUID().toString());

    MDC.put(MDC_KEY, correlationId);
    resp.setHeader(CORRELATION_ID_HEADER, correlationId);

    try {
      chain.doFilter(request, response);
    } finally {
      MDC.remove(MDC_KEY);
    }
  }
}
