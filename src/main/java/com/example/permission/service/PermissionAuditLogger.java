package com.example.permission.service;

import com.auditlog.api.AuditActorType;
import com.auditlog.api.AuditEvent;
import com.auditlog.api.AuditEventType;
import com.auditlog.api.AuditLogger;
import com.auditlog.api.AuditResult;
import com.auditlog.api.AuditSink;
import com.auditlog.core.DefaultAuditLogger;
import com.auditlog.core.FileAuditSink;
import com.example.permission.domain.Decision;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.List;

@Component
public class PermissionAuditLogger {
    private final boolean enabled;
    private final AuditLogger logger;

    public PermissionAuditLogger(
            @Value("${auditlog.enabled:true}") boolean enabled,
            @Value("${auditlog.service-name:authz-service}") String serviceName,
            @Value("${auditlog.env:local}") String env,
            @Value("${auditlog.file-path:./logs/audit.log}") String filePath
    ) {
        this.enabled = enabled;
        if (!enabled) {
            this.logger = null;
            return;
        }

        AuditSink sink = new FileAuditSink(Path.of(filePath), serviceName, env);
        this.logger = new DefaultAuditLogger(sink, List.of(), null);
    }

    public void log(String requestId,
                    String correlationId,
                    String userId,
                    String method,
                    String path,
                    Decision decision,
                    String reason,
                    long latencyMs) {
        if (!enabled || logger == null) {
            return;
        }

        AuditEvent event = AuditEvent.builder(AuditEventType.CUSTOM, "AUTHZ_ADMIN_VERIFY")
                .actor(valueOrDash(userId), valueOrAnonymous(userId), null)
                .resource("HTTP_PATH", valueOrDash(path))
                .result(mapResult(decision))
                .reason(valueOrDash(reason))
                .traceId(valueOrDash(requestId))
                .requestId(valueOrDash(requestId))
                .detail("correlationId", valueOrDash(correlationId))
                .detail("method", valueOrDash(method))
                .detail("latencyMs", String.valueOf(latencyMs))
                .build();
        logger.log(event);
    }

    private static AuditResult mapResult(Decision decision) {
        return decision == Decision.ALLOW ? AuditResult.SUCCESS : AuditResult.FAILURE;
    }

    private static String valueOrDash(String value) {
        if (value == null || value.isBlank()) {
            return "-";
        }
        return value;
    }

    private static AuditActorType valueOrAnonymous(String userId) {
        if (userId == null || userId.isBlank()) {
            return AuditActorType.ANONYMOUS;
        }
        return AuditActorType.USER;
    }
}
