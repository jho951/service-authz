package com.authzservice.app.domain.audit;

import com.auditlog.api.AuditActorType;
import com.auditlog.api.AuditEvent;
import com.auditlog.api.AuditEventType;
import com.auditlog.api.AuditLogger;
import com.auditlog.api.AuditResult;
import com.authzservice.app.domain.authorization.model.Decision;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class PermissionAuditLogger {
    private final boolean enabled;
    private final AuditLogger auditLogger;

    public PermissionAuditLogger(
            @Value("${auditlog.enabled:true}") boolean enabled,
            AuditLogger auditLogger
    ) {
        this.enabled = enabled;
        this.auditLogger = auditLogger;
    }

    public void log(String requestId,
                    String correlationId,
                    String userId,
                    String method,
                    String path,
                    Decision decision,
                    String reason,
                    long latencyMs) {
        if (!enabled) {
            return;
        }

        Map<String, String> attributes = new LinkedHashMap<>();
        attributes.put("requestId", valueOrDash(requestId));
        attributes.put("correlationId", valueOrDash(correlationId));
        attributes.put("userId", valueOrDash(userId));
        attributes.put("method", valueOrDash(method));
        attributes.put("path", valueOrDash(path));
        attributes.put("decision", decision == null ? "UNKNOWN" : decision.name());
        attributes.put("reason", valueOrDash(reason));
        attributes.put("latencyMs", String.valueOf(latencyMs));

        auditLogger.log(
                AuditEvent.builder(AuditEventType.READ, "AUTHZ_ADMIN_VERIFY")
                        .occurredAt(Instant.now())
                        .actor(valueOrDash(userId), AuditActorType.USER, valueOrDash(userId))
                        .resource("AUTHZ_POLICY", valueOrDash(path))
                        .result(decision == Decision.ALLOW ? AuditResult.SUCCESS : AuditResult.FAILURE)
                        .reason(valueOrDash(reason))
                        .requestId(valueOrDash(requestId))
                        .traceId(valueOrDash(correlationId))
                        .details(new LinkedHashMap<>(attributes))
                        .build()
        );
    }

    private static String valueOrDash(String value) {
        if (value == null || value.isBlank()) {
            return "-";
        }
        return value;
    }
}
