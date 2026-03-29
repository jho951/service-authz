package com.example.permission.api;

public record AdminPermissionVerifyRequest(
        String userId,
        String userRole,
        String originalMethod,
        String originalPath,
        String requestId,
        String correlationId
) {
}
