package com.authzservice.common.base.constant;

/**
 * authz-service success response metadata.
 */
public enum SuccessCode {
    /** Admin permission verification allowed. */
    AUTHZ_ADMIN_VERIFY_ALLOWED(200, 4000, "관리자 권한 검증 허용"),

    /** Health check succeeded. */
    HEALTH_CHECK_SUCCESS(200, 4001, "상태 확인 성공"),

    /** Readiness check succeeded. */
    READINESS_CHECK_SUCCESS(200, 4002, "준비 상태 확인 성공");

    private final int httpStatus;
    private final int code;
    private final String message;

    SuccessCode(int httpStatus, int code, String message) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.message = message;
    }

    public int getHttpStatus() {
        return httpStatus;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
