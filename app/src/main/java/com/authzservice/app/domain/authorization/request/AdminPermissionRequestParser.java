package com.authzservice.app.domain.authorization.request;

import com.authzservice.app.domain.authorization.dto.AdminPermissionVerifyRequest;
import com.authzservice.app.domain.authorization.support.PermissionHeaderNames;
import com.authzservice.common.base.constant.ErrorCode;
import com.authzservice.common.base.exception.GlobalException;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Component
public class AdminPermissionRequestParser {
    private static final Set<String> ALLOWED_METHODS = Set.of("GET", "HEAD", "OPTIONS", "POST", "PUT", "PATCH", "DELETE");

    public AdminPermissionVerifyRequest parse(Map<String, String> headers) {
        String userId = requiredHeader(headers, PermissionHeaderNames.USER_ID, "X-User-Id 헤더가 필요합니다.");
        String originalMethod = normalizeMethod(requiredHeader(headers, PermissionHeaderNames.ORIGINAL_METHOD, "X-Original-Method 헤더가 필요합니다."));
        String originalPath = normalizePath(requiredHeader(headers, PermissionHeaderNames.ORIGINAL_PATH, "X-Original-Path 헤더가 필요합니다."));

        String requestId = optionalHeader(headers, PermissionHeaderNames.REQUEST_ID);
        String correlationId = optionalHeader(headers, PermissionHeaderNames.CORRELATION_ID);

        return new AdminPermissionVerifyRequest(
                userId,
                originalMethod,
                originalPath,
                requestId,
                correlationId
        );
    }

    private String requiredHeader(Map<String, String> headers, String key, String message) {
        String value = optionalHeader(headers, key);
        if (value == null || value.isBlank()) {
            throw new GlobalException(ErrorCode.INVALID_REQUEST, message);
        }
        return value;
    }

    private String optionalHeader(Map<String, String> headers, String key) {
        String value = findHeaderIgnoreCase(headers, key);
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        return trimmed;
    }

    private String findHeaderIgnoreCase(Map<String, String> headers, String key) {
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(key)) {
                return entry.getValue();
            }
        }
        return null;
    }

    private String normalizeMethod(String method) {
        String normalized = method.toUpperCase(Locale.ROOT);
        if (!ALLOWED_METHODS.contains(normalized)) {
            throw new GlobalException(ErrorCode.INVALID_REQUEST, "허용하지 않는 HTTP Method입니다.");
        }
        return normalized;
    }

    private String normalizePath(String path) {
        if (!path.startsWith("/")) {
            throw new GlobalException(ErrorCode.INVALID_REQUEST, "X-Original-Path는 '/'로 시작해야 합니다.");
        }
        if (path.contains("..")) {
            throw new GlobalException(ErrorCode.INVALID_REQUEST, "X-Original-Path에 '..'를 포함할 수 없습니다.");
        }
        if (path.indexOf('\n') >= 0 || path.indexOf('\r') >= 0) {
            throw new GlobalException(ErrorCode.INVALID_REQUEST, "X-Original-Path 형식이 잘못되었습니다.");
        }
        return path;
    }
}
