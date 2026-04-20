package com.authzservice.app.domain.authorization.support;

public final class PermissionHeaderNames {
    private PermissionHeaderNames() {
    }

    public static final String USER_ID = "X-User-Id";
    public static final String ORIGINAL_METHOD = "X-Original-Method";
    public static final String ORIGINAL_PATH = "X-Original-Path";
    public static final String REQUEST_ID = "X-Request-Id";
    public static final String CORRELATION_ID = "X-Correlation-Id";
    public static final String INTERNAL_REQUEST_SECRET = "X-Internal-Request-Secret";
    public static final String AUTHORIZATION = "Authorization";
}
