package com.authzservice.app.domain.audit;

import com.authzservice.app.domain.authorization.model.Decision;
import io.github.jho951.platform.governance.api.AuditEntry;
import io.github.jho951.platform.governance.api.AuditLogRecorder;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class PermissionAuditLogger {
    private final boolean enabled;
    private final AuditLogRecorder recorder;

    public PermissionAuditLogger(
            @Value("${auditlog.enabled:true}") boolean enabled,
            AuditLogRecorder recorder
    ) {
        this.enabled = enabled;
        this.recorder = recorder;
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

        recorder.record(new AuditEntry("authz", "AUTHZ_ADMIN_VERIFY", attributes, Instant.now()));
    }

    private static String valueOrDash(String value) {
        if (value == null || value.isBlank()) {
            return "-";
        }
        return value;
    }
}
