package com.authzservice.app.domain.audit;

import com.auditlog.api.AuditActorType;
import com.auditlog.api.AuditEvent;
import com.auditlog.api.AuditEventType;
import com.auditlog.api.AuditResult;
import com.auditlog.api.AuditSink;
import com.authzservice.app.domain.authorization.model.Decision;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

@Component
public class PermissionAuditLogger {
    private final boolean enabled;
    private final AuditSink auditSink;

    public PermissionAuditLogger(
            @Value("${auditlog.enabled:true}") boolean enabled,
            ObjectProvider<AuditSink> auditSinkProvider
    ) {
        this.enabled = enabled;
        this.auditSink = auditSinkProvider.getIfAvailable(() -> event -> { });
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
        attributes.put("result", decision == Decision.ALLOW ? "SUCCESS" : "FAILURE");
        attributes.put("reason", valueOrDash(reason));
        attributes.put("latencyMs", String.valueOf(latencyMs));

        auditSink.write(AuditEvent.builder(AuditEventType.CUSTOM, "AUTHZ_ADMIN_VERIFY")
                .occurredAt(Instant.now())
                .actor(
                        valueOrUnknown(userId),
                        resolveActorType(userId),
                        valueOrUnknown(userId)
                )
                .resource("authz.permission", valueOrUnknown(path))
                .result(decision == Decision.ALLOW ? AuditResult.SUCCESS : AuditResult.FAILURE)
                .reason(valueOrDash(reason))
                .details(attributes)
                .build());
    }

    private static String valueOrDash(String value) {
        if (value == null || value.isBlank()) {
            return "-";
        }
        return value;
    }

    private static String valueOrUnknown(String value) {
        if (value == null || value.isBlank() || "-".equals(value)) {
            return "unknown";
        }
        return value;
    }

    private static AuditActorType resolveActorType(String userId) {
        return userId == null || userId.isBlank() ? AuditActorType.UNKNOWN : AuditActorType.USER;
    }
}
