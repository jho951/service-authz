package com.example.enroll.api;

import java.util.Map;

public class BusinessException extends RuntimeException {
    private final ErrorCode errorCode;
    private final Map<String, Object> detail;

    public BusinessException(ErrorCode errorCode) {
        this(errorCode, errorCode.defaultMessage(), Map.of());
    }

    public BusinessException(ErrorCode errorCode, String message, Map<String, Object> detail) {
        super(message);
        this.errorCode = errorCode;
        this.detail = detail == null ? Map.of() : detail;
    }

    public ErrorCode errorCode() {
        return errorCode;
    }

    public Map<String, Object> detail() {
        return detail;
    }
}
