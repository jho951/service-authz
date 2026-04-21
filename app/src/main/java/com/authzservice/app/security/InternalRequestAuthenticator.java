package com.authzservice.app.security;

import com.authzservice.app.domain.authorization.support.PermissionHeaderNames;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.stereotype.Component;

@Component
public class InternalRequestAuthenticator {

    private static final TypeReference<Map<String, Object>> CLAIMS_TYPE = new TypeReference<>() {};
    private final InternalRequestAuthenticationProperties properties;
    private final ObjectMapper objectMapper;

    public InternalRequestAuthenticator(InternalRequestAuthenticationProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    public InternalRequestAuthenticationResult authenticate(Map<String, String> headers) {
        return switch (properties.getMode()) {
            case DISABLED -> InternalRequestAuthenticationResult.allow("INTERNAL_AUTH_DISABLED");
            case LEGACY_SECRET -> authenticateLegacySecret(headers);
            case JWT -> authenticateJwt(headers);
            case HYBRID -> authenticateHybrid(headers);
        };
    }

    private InternalRequestAuthenticationResult authenticateHybrid(Map<String, String> headers) {
        InternalRequestAuthenticationResult jwtResult = authenticateJwt(headers);
        if (jwtResult.allowed()) {
            return jwtResult;
        }
        InternalRequestAuthenticationResult legacyResult = authenticateLegacySecret(headers);
        if (legacyResult.allowed()) {
            return legacyResult;
        }
        return InternalRequestAuthenticationResult.deny(jwtResult.reason() + "," + legacyResult.reason());
    }

    private InternalRequestAuthenticationResult authenticateLegacySecret(Map<String, String> headers) {
        String expected = properties.getLegacySecret();
        if (expected == null || expected.isBlank()) {
            return InternalRequestAuthenticationResult.deny("LEGACY_SECRET_NOT_CONFIGURED");
        }
        String provided = resolveHeader(headers, PermissionHeaderNames.INTERNAL_REQUEST_SECRET);
        if (constantTimeEquals(expected, provided)) {
            return InternalRequestAuthenticationResult.allow("LEGACY_SECRET_MATCH");
        }
        return InternalRequestAuthenticationResult.deny("INVALID_LEGACY_SECRET");
    }

    private InternalRequestAuthenticationResult authenticateJwt(Map<String, String> headers) {
        String authorization = resolveHeader(headers, PermissionHeaderNames.AUTHORIZATION);
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return InternalRequestAuthenticationResult.deny("JWT_MISSING");
        }
        return authenticateAccessToken(authorization.substring("Bearer ".length()).trim());
    }

    public InternalRequestAuthenticationResult authenticateAccessToken(String token) {
        String secret = properties.getJwt().getSecret();
        if (secret == null || secret.isBlank()) {
            return InternalRequestAuthenticationResult.deny("JWT_SECRET_NOT_CONFIGURED");
        }
        if (token == null || token.isBlank()) {
            return InternalRequestAuthenticationResult.deny("JWT_MISSING");
        }

        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                return InternalRequestAuthenticationResult.deny("JWT_MALFORMED");
            }
            if (!constantTimeEquals(sign(parts[0] + "." + parts[1], secret), parts[2])) {
                return InternalRequestAuthenticationResult.deny("JWT_BAD_SIGNATURE");
            }

            Map<String, Object> claims = objectMapper.readValue(Base64.getUrlDecoder().decode(parts[1]), CLAIMS_TYPE);
            return validateClaims(claims);
        } catch (Exception ex) {
            return InternalRequestAuthenticationResult.deny("JWT_INVALID");
        }
    }

    private InternalRequestAuthenticationResult validateClaims(Map<String, Object> claims) {
        long now = Instant.now().getEpochSecond();
        long skew = properties.getJwt().getClockSkewSeconds();

        if (!matchesStringClaim(claims.get("iss"), properties.getJwt().getIssuer())) {
            return InternalRequestAuthenticationResult.deny("JWT_BAD_ISSUER");
        }
        if (!matchesAudience(claims.get("aud"), properties.getJwt().getAudience())) {
            return InternalRequestAuthenticationResult.deny("JWT_BAD_AUDIENCE");
        }
        Long exp = numericClaim(claims.get("exp"));
        if (exp == null || exp + skew < now) {
            return InternalRequestAuthenticationResult.deny("JWT_EXPIRED");
        }
        Long nbf = numericClaim(claims.get("nbf"));
        if (nbf != null && nbf - skew > now) {
            return InternalRequestAuthenticationResult.deny("JWT_NOT_BEFORE");
        }
        return InternalRequestAuthenticationResult.allow("JWT_VALID");
    }

    private boolean matchesStringClaim(Object actual, String expected) {
        return expected == null || expected.isBlank() || expected.equals(actual);
    }

    private boolean matchesAudience(Object actual, String expected) {
        if (expected == null || expected.isBlank()) {
            return true;
        }
        if (actual instanceof String value) {
            return expected.equals(value);
        }
        if (actual instanceof List<?> values) {
            return values.stream().anyMatch(expected::equals);
        }
        return false;
    }

    private Long numericClaim(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        return null;
    }

    private String sign(String data, String secret) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        return Base64.getUrlEncoder().withoutPadding().encodeToString(mac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
    }

    private boolean constantTimeEquals(String expected, String provided) {
        if (expected == null || provided == null) {
            return false;
        }
        return MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8), provided.getBytes(StandardCharsets.UTF_8));
    }

    private String resolveHeader(Map<String, String> headers, String key) {
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(key)) {
                String value = entry.getValue();
                return value == null || value.isBlank() ? null : value.trim();
            }
        }
        return null;
    }
}
