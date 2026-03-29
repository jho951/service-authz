package com.example.permission.service;

import com.example.permission.domain.Decision;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class PermissionAuditLogger {
    private static final Logger log = LoggerFactory.getLogger("permission.audit");

    public void log(String requestId,
                    String correlationId,
                    String userId,
                    String method,
                    String path,
                    Decision decision,
                    String reason,
                    long latencyMs) {
        log.info(
                "requestId={} correlationId={} userId={} method={} path={} decision={} reason={} latencyMs={}",
                valueOrDash(requestId),
                valueOrDash(correlationId),
                valueOrDash(userId),
                valueOrDash(method),
                valueOrDash(path),
                decision,
                valueOrDash(reason),
                latencyMs
        );
    }

    private String valueOrDash(String value) {
        if (value == null || value.isBlank()) {
            return "-";
        }
        return value;
    }
}
