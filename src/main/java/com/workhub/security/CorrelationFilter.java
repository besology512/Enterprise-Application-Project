package com.workhub.security;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorrelationFilter implements Filter {

    private static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    private static final String CORRELATION_ID_LOG_VAR = "correlationId";

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(CorrelationFilter.class);

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (request instanceof HttpServletRequest httpServletRequest) {
            String correlationId = httpServletRequest.getHeader(CORRELATION_ID_HEADER);
            if (correlationId == null || correlationId.isEmpty()) {
                correlationId = UUID.randomUUID().toString();
            }
            MDC.put(CORRELATION_ID_LOG_VAR, correlationId);
            logger.debug("Correlation ID set to: {}", correlationId);
            if (response instanceof HttpServletResponse httpServletResponse) {
                httpServletResponse.addHeader(CORRELATION_ID_HEADER, correlationId);
            }
        }
        try {
            chain.doFilter(request, response);
        } finally {
            MDC.remove(CORRELATION_ID_LOG_VAR);
        }
    }
}
