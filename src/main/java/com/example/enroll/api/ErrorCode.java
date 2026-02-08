package com.example.enroll.api;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    REG_CAPACITY_FULL(HttpStatus.CONFLICT, "정원이 가득 찼습니다."),
    REG_MAX_CREDITS(HttpStatus.CONFLICT, "최대 학점을 초과했습니다."),
    REG_TIME_CONFLICT(HttpStatus.CONFLICT, "시간표가 충돌합니다."),
    REG_DUPLICATE_COURSE(HttpStatus.CONFLICT, "이미 신청한 강좌입니다."),
    REG_NOT_FOUND(HttpStatus.NOT_FOUND, "대상이 존재하지 않습니다."),
    REG_ALREADY_CANCELED(HttpStatus.CONFLICT, "이미 취소된 신청입니다.");

    private final HttpStatus status;
    private final String defaultMessage;

    ErrorCode(HttpStatus status, String defaultMessage) {
        this.status = status;
        this.defaultMessage = defaultMessage;
    }

    public HttpStatus status() {
        return status;
    }

    public String defaultMessage() {
        return defaultMessage;
    }
}
