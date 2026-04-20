package com.authzservice.app.domain.authorization.dto;

public record AdminPermissionVerifyRequest(
        String userId,
        String originalMethod,
        String originalPath,
        String requestId,
        String correlationId
) {
}
