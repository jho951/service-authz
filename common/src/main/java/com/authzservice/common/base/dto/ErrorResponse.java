package com.authzservice.common.base.dto;

public record ErrorResponse(
        int code,
        String message
) {
}
