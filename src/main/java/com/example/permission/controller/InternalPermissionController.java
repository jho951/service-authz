package com.example.permission.controller;

import com.example.permission.api.AdminPermissionVerifyRequest;
import com.example.permission.api.PermissionBadRequestException;
import com.example.permission.api.PermissionHeaderNames;
import com.example.permission.domain.Decision;
import com.example.permission.service.AdminPermissionRequestParser;
import com.example.permission.service.AdminPermissionVerifier;
import com.example.permission.service.PermissionAuditLogger;
import com.example.permission.service.PermissionDecisionResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/permissions/internal/admin")
public class InternalPermissionController {

    private final AdminPermissionRequestParser requestParser;
    private final AdminPermissionVerifier verifier;
    private final PermissionAuditLogger auditLogger;
    private final String internalRequestSecret;

    public InternalPermissionController(AdminPermissionRequestParser requestParser,
                                        AdminPermissionVerifier verifier,
                                        PermissionAuditLogger auditLogger,
                                        @Value("${permission.internal-request-secret:}") String internalRequestSecret) {
        this.requestParser = requestParser;
        this.verifier = verifier;
        this.auditLogger = auditLogger;
        this.internalRequestSecret = internalRequestSecret;
    }

    @PostMapping("/verify")
    public ResponseEntity<Void> verify(@RequestHeader Map<String, String> headers) {
        long startedAt = System.currentTimeMillis();
        String requestId = resolveTraceHeader(headers, PermissionHeaderNames.REQUEST_ID);
        String correlationId = resolveTraceHeader(headers, PermissionHeaderNames.CORRELATION_ID);
        String userId = resolveHeader(headers, PermissionHeaderNames.USER_ID);
        String method = resolveHeader(headers, PermissionHeaderNames.ORIGINAL_METHOD);
        String path = resolveHeader(headers, PermissionHeaderNames.ORIGINAL_PATH);

        if (isInternalSecretInvalid(headers)) {
            auditLogger.log(
                    requestId,
                    correlationId,
                    userId,
                    method,
                    path,
                    Decision.DENY,
                    "INVALID_INTERNAL_SECRET",
                    System.currentTimeMillis() - startedAt
            );
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .header(PermissionHeaderNames.REQUEST_ID, requestId)
                    .header(PermissionHeaderNames.CORRELATION_ID, correlationId)
                    .build();
        }

        try {
            AdminPermissionVerifyRequest parsed = requestParser.parse(headers);
            AdminPermissionVerifyRequest request = new AdminPermissionVerifyRequest(
                    parsed.userId(),
                    parsed.userRole(),
                    parsed.originalMethod(),
                    parsed.originalPath(),
                    requestId,
                    correlationId
            );
            PermissionDecisionResult result = verifier.verify(request);

            auditLogger.log(
                    request.requestId(),
                    request.correlationId(),
                    request.userId(),
                    request.originalMethod(),
                    request.originalPath(),
                    result.decision(),
                    result.reason(),
                    System.currentTimeMillis() - startedAt
            );

            if (result.isAllowed()) {
                return ResponseEntity.ok()
                        .header(PermissionHeaderNames.REQUEST_ID, requestId)
                        .header(PermissionHeaderNames.CORRELATION_ID, correlationId)
                        .build();
            }
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .header(PermissionHeaderNames.REQUEST_ID, requestId)
                    .header(PermissionHeaderNames.CORRELATION_ID, correlationId)
                    .build();
        } catch (PermissionBadRequestException ex) {
            auditLogger.log(
                    requestId,
                    correlationId,
                    userId,
                    method,
                    path,
                    Decision.DENY,
                    "BAD_REQUEST",
                    System.currentTimeMillis() - startedAt
            );
            throw ex;
        }
    }

    private String resolveTraceHeader(Map<String, String> headers, String key) {
        String header = resolveHeader(headers, key);
        if (header == null || header.isBlank()) {
            return UUID.randomUUID().toString();
        }
        return header;
    }

    private boolean isInternalSecretInvalid(Map<String, String> headers) {
        if (internalRequestSecret == null || internalRequestSecret.isBlank()) {
            return false;
        }
        String provided = resolveHeader(headers, PermissionHeaderNames.INTERNAL_REQUEST_SECRET);
        return !internalRequestSecret.equals(provided);
    }

    private String resolveHeader(Map<String, String> headers, String key) {
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            if (!entry.getKey().equalsIgnoreCase(key)) {
                continue;
            }
            String value = entry.getValue();
            if (value == null || value.isBlank()) {
                return null;
            }
            return value.trim();
        }
        return null;
    }
}
