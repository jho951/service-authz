package com.authzservice.common.base.constant;

public enum ErrorCode {
    INVALID_REQUEST(400, 9001, "잘못된 요청입니다."),
    UNAUTHORIZED(401, 9002, "인증이 필요합니다."),
    FORBIDDEN(403, 9003, "접근이 허용되지 않습니다."),
    NOT_FOUND(404, 9004, "요청한 리소스를 찾을 수 없습니다."),
    METHOD_NOT_ALLOWED(405, 9005, "허용하지 않는 HTTP Method입니다."),
    TOO_MANY_REQUESTS(429, 9006, "요청이 너무 많습니다."),
    INTERNAL_ERROR(500, 9999, "서버 오류가 발생했습니다.");

    private final int httpStatus;
    private final int code;
    private final String message;

    ErrorCode(int httpStatus, int code, String message) {
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
