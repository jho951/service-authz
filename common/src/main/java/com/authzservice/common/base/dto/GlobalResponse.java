package com.authzservice.common.base.dto;

import com.authzservice.common.base.constant.ErrorCode;
import com.authzservice.common.base.constant.SuccessCode;
import org.springframework.http.ResponseEntity;

/**
 * Common API response wrapper.
 *
 * @param <T> response data type
 */
public final class GlobalResponse<T> {
    private final int httpStatus;
    private final boolean success;
    private final String message;
    private final int code;
    private final T data;

    private GlobalResponse(int httpStatus, boolean success, String message, int code, T data) {
        if (message == null) {
            throw new IllegalArgumentException("메시지는 null일 수 없습니다.");
        }
        this.httpStatus = httpStatus;
        this.success = success;
        this.message = message;
        this.code = code;
        this.data = data;
    }

    public static <T> GlobalResponse<T> ok(SuccessCode successCode, T data) {
        if (successCode == null) {
            throw new IllegalArgumentException("성공 코드는 null일 수 없습니다.");
        }
        return new GlobalResponse<>(
                successCode.getHttpStatus(),
                true,
                successCode.getMessage(),
                successCode.getCode(),
                data
        );
    }

    public static GlobalResponse<Void> ok(SuccessCode successCode) {
        if (successCode == null) {
            throw new IllegalArgumentException("성공 코드는 null일 수 없습니다.");
        }
        return new GlobalResponse<>(
                successCode.getHttpStatus(),
                true,
                successCode.getMessage(),
                successCode.getCode(),
                null
        );
    }

    public static <T> ResponseEntity<GlobalResponse<T>> success(SuccessCode successCode, T data) {
        return ResponseEntity
                .status(successCode.getHttpStatus())
                .body(ok(successCode, data));
    }

    public static ResponseEntity<GlobalResponse<Void>> success(SuccessCode successCode) {
        return ResponseEntity
                .status(successCode.getHttpStatus())
                .body(ok(successCode));
    }

    public static GlobalResponse<Void> fail(ErrorCode errorCode) {
        return fail(errorCode, errorCode.getMessage());
    }

    public static GlobalResponse<Void> fail(ErrorCode errorCode, String message) {
        if (errorCode == null) {
            throw new IllegalArgumentException("에러 코드는 null일 수 없습니다.");
        }
        return new GlobalResponse<>(
                errorCode.getHttpStatus(),
                false,
                message,
                errorCode.getCode(),
                null
        );
    }

    public int getHttpStatus() {
        return httpStatus;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public int getCode() {
        return code;
    }

    public T getData() {
        return data;
    }
}
