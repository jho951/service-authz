package com.example.enroll.api;

import java.util.Map;

public record ErrorResponse(ErrorBody error) {
    public static ErrorResponse of(ErrorCode code, String message, Map<String, Object> detail) {
        return new ErrorResponse(new ErrorBody(code.name(), message, detail));
    }

    public record ErrorBody(String code, String message, Map<String, Object> detail) {
    }
}
