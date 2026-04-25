package com.authzservice.app.domain.audit;

import com.authzservice.app.domain.authorization.model.Decision;
import io.github.jho951.platform.governance.api.AuditEntry;
import io.github.jho951.platform.governance.api.GovernanceAuditRecorder;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class PermissionAuditLogger {
    private final boolean enabled;
    private final GovernanceAuditRecorder auditRecorder;

    public PermissionAuditLogger(
            @Value("${auditlog.enabled:true}") boolean enabled,
            ObjectProvider<GovernanceAuditRecorder> auditRecorderProvider
    ) {
        this.enabled = enabled;
        this.auditRecorder = auditRecorderProvider.getIfAvailable(() -> entry -> { });
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
        attributes.put("actorType", userId == null || userId.isBlank() ? "UNKNOWN" : "USER");

        auditRecorder.record(new AuditEntry(
                "authz.permission",
                "AUTHZ_ADMIN_VERIFY",
                Map.copyOf(attributes),
                Instant.now()
        ));
    }

    private static String valueOrDash(String value) {
        if (value == null || value.isBlank()) {
            return "-";
        }
        return value;
    }
}
