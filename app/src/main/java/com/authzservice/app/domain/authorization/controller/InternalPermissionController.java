package com.authzservice.app.domain.authorization.controller;

import com.authzservice.app.domain.authorization.dto.AdminPermissionVerifyRequest;
import com.authzservice.app.domain.authorization.model.Decision;
import com.authzservice.app.domain.authorization.model.PermissionDecisionResult;
import com.authzservice.app.domain.authorization.request.AdminPermissionRequestParser;
import com.authzservice.app.domain.authorization.service.AdminPermissionVerifier;
import com.authzservice.app.domain.authorization.support.PermissionHeaderNames;
import com.authzservice.app.domain.audit.PermissionAuditLogger;
import com.authzservice.app.security.InternalRequestAuthenticationResult;
import com.authzservice.app.security.InternalRequestAuthenticator;
import com.authzservice.common.base.constant.ErrorCode;
import com.authzservice.common.base.exception.GlobalException;
import com.authzservice.common.swagger.SwaggerTag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = SwaggerTag.INTERNAL_PERMISSION, description = "Gateway internal admin permission verification APIs")
public class InternalPermissionController {

    private final AdminPermissionRequestParser requestParser;
    private final AdminPermissionVerifier verifier;
    private final PermissionAuditLogger auditLogger;
    private final InternalRequestAuthenticator internalRequestAuthenticator;

    public InternalPermissionController(AdminPermissionRequestParser requestParser,
                                        AdminPermissionVerifier verifier,
                                        PermissionAuditLogger auditLogger,
                                        InternalRequestAuthenticator internalRequestAuthenticator) {
        this.requestParser = requestParser;
        this.verifier = verifier;
        this.auditLogger = auditLogger;
        this.internalRequestAuthenticator = internalRequestAuthenticator;
    }

    @PostMapping("/verify")
    @Operation(
            summary = "Verify admin permission",
            description = "Gateway calls this endpoint before forwarding admin requests. Authz evaluates the original user, method, and path against its role-permission model.",
            security = {
                    @SecurityRequirement(name = "internalJwt"),
                    @SecurityRequirement(name = "internalSecret")
            },
            parameters = {
                    @Parameter(name = PermissionHeaderNames.AUTHORIZATION, in = ParameterIn.HEADER, description = "Bearer internal service JWT for JWT/HYBRID internal auth modes"),
                    @Parameter(name = PermissionHeaderNames.INTERNAL_REQUEST_SECRET, in = ParameterIn.HEADER, description = "Legacy shared internal secret for LEGACY_SECRET/HYBRID internal auth modes"),
                    @Parameter(name = PermissionHeaderNames.USER_ID, in = ParameterIn.HEADER, required = true, description = "Authenticated user id forwarded by Gateway"),
                    @Parameter(name = PermissionHeaderNames.ORIGINAL_METHOD, in = ParameterIn.HEADER, required = true, description = "Original HTTP method requested by the client"),
                    @Parameter(name = PermissionHeaderNames.ORIGINAL_PATH, in = ParameterIn.HEADER, required = true, description = "Original request path requested by the client"),
                    @Parameter(name = PermissionHeaderNames.REQUEST_ID, in = ParameterIn.HEADER, description = "Request trace id. Generated when omitted."),
                    @Parameter(name = PermissionHeaderNames.CORRELATION_ID, in = ParameterIn.HEADER, description = "Cross-service correlation id. Generated when omitted.")
            }
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Permission allowed"),
            @ApiResponse(responseCode = "400", description = "Required verification header is missing or invalid"),
            @ApiResponse(responseCode = "403", description = "Internal authentication failed or permission is denied")
    })
    public ResponseEntity<Void> verify(@RequestHeader Map<String, String> headers) {
        long startedAt = System.currentTimeMillis();
        String requestId = resolveTraceHeader(headers, PermissionHeaderNames.REQUEST_ID);
        String correlationId = resolveTraceHeader(headers, PermissionHeaderNames.CORRELATION_ID);
        String userId = resolveHeader(headers, PermissionHeaderNames.USER_ID);
        String method = resolveHeader(headers, PermissionHeaderNames.ORIGINAL_METHOD);
        String path = resolveHeader(headers, PermissionHeaderNames.ORIGINAL_PATH);

        InternalRequestAuthenticationResult authenticationResult = internalRequestAuthenticator.authenticate(headers);
        if (!authenticationResult.allowed()) {
            auditLogger.log(
                    requestId,
                    correlationId,
                    userId,
                    method,
                    path,
                    Decision.DENY,
                    authenticationResult.reason(),
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
        } catch (GlobalException ex) {
            if (ex.getErrorCode() != ErrorCode.INVALID_REQUEST) {
                throw ex;
            }
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
