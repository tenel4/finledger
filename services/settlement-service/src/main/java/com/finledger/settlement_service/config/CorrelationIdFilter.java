package com.finledger.settlement_service.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE) // Ensures this runs before other filters
public class CorrelationIdFilter implements Filter {

    public static final String CORRELATION_ID_HEADER = "X-Correlation-Id";
    public static final String MDC_KEY = "correlationId";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        if (!(request instanceof HttpServletRequest httpRequest) || !(response instanceof HttpServletResponse httpResponse)) {
            chain.doFilter(request, response);
            return;
        }

        // Get existing correlation ID or generate a new one
        String correlationId = Optional.ofNullable(httpRequest.getHeader(CORRELATION_ID_HEADER))
                .filter(id -> !id.isBlank())
                .orElse(UUID.randomUUID().toString());

        // Put into MDC for logging
        MDC.put(MDC_KEY, correlationId);

        // Set in response so client knows the ID used
        httpResponse.setHeader(CORRELATION_ID_HEADER, correlationId);

        // Wrap request so downstream code sees the header
        HttpServletRequest wrappedRequest = new HttpServletRequestWrapper(httpRequest) {
            @Override
            public String getHeader(String name) {
                if (CORRELATION_ID_HEADER.equalsIgnoreCase(name)) {
                    return correlationId;
                }
                return super.getHeader(name);
            }
        };

        try {
            chain.doFilter(wrappedRequest, response);
        } finally {
            MDC.remove(MDC_KEY); // Prevent leakage between threads
        }
    }
}
