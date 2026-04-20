package com.authzservice.app.config.logging;

import com.authzservice.common.logging.LoggingHeaders;
import com.authzservice.common.logging.LoggingMdcKeys;
import com.authzservice.common.logging.SensitiveDataMasker;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class RequestAccessLogFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RequestAccessLogFilter.class);

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return request.getRequestURI().startsWith("/actuator");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        long startedAt = System.currentTimeMillis();

        try {
            filterChain.doFilter(request, response);
        } finally {
            long elapsedMs = System.currentTimeMillis() - startedAt;
            log.info(
                    "http_access method={} path={} query={} status={} durationMs={} forwardedFor={} requestId={} correlationId={} gatewayUserId={}",
                    request.getMethod(),
                    request.getRequestURI(),
                    SensitiveDataMasker.maskQuery(request.getQueryString()),
                    response.getStatus(),
                    elapsedMs,
                    request.getHeader(LoggingHeaders.X_FORWARDED_FOR),
                    MDC.get(LoggingMdcKeys.REQUEST_ID),
                    MDC.get(LoggingMdcKeys.CORRELATION_ID),
                    SensitiveDataMasker.maskIdentifier(request.getHeader(LoggingHeaders.X_USER_ID))
            );
        }
    }
}
