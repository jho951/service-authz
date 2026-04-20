package com.authzservice.app.security;

public record InternalRequestAuthenticationResult(boolean allowed, String reason) {

    public static InternalRequestAuthenticationResult allow(String reason) {
        return new InternalRequestAuthenticationResult(true, reason);
    }

    public static InternalRequestAuthenticationResult deny(String reason) {
        return new InternalRequestAuthenticationResult(false, reason);
    }
}
