package com.authzservice.common.base.dto;

public record BaseResponse<T>(
        boolean succeeded,
        T data,
        String message
) {
    public static <T> BaseResponse<T> success(T data) {
        return new BaseResponse<>(true, data, null);
    }

    public static BaseResponse<Void> success() {
        return new BaseResponse<>(true, null, null);
    }

    public static BaseResponse<Void> failure(String message) {
        return new BaseResponse<>(false, null, message);
    }
}
