package com.example.permission.service;

import com.example.permission.domain.Decision;

public record PermissionDecisionResult(Decision decision, String reason) {

    public static PermissionDecisionResult allow(String reason) {
        return new PermissionDecisionResult(Decision.ALLOW, reason);
    }

    public static PermissionDecisionResult deny(String reason) {
        return new PermissionDecisionResult(Decision.DENY, reason);
    }

    public boolean isAllowed() {
        return decision == Decision.ALLOW;
    }
}
