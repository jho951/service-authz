package com.authzservice.app.security;

import io.github.jho951.platform.security.api.SecurityContext;
import io.github.jho951.platform.security.api.SecurityRequest;
import io.github.jho951.platform.security.auth.InternalServiceCompatibilityAuthenticationAdapter;
import io.github.jho951.platform.security.auth.PlatformAuthenticatedPrincipal;
import io.github.jho951.platform.security.auth.PlatformSessionSupport;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

@Component
public class AuthzInternalRequestAuthorizer {

    private static final String INTERNAL_PRINCIPAL = "authz-internal-caller";
    private static final Set<String> INTERNAL_AUTHORITIES = Set.of("ROLE_INTERNAL");

    private final PlatformSessionSupport platformSessionSupport;
    private final InternalRequestAuthenticator internalRequestAuthenticator;
    private final InternalRequestAuthenticationProperties properties;
    private final InternalServiceCompatibilityAuthenticationAdapter compatibilityAuthenticationAdapter;

    public AuthzInternalRequestAuthorizer(
        PlatformSessionSupport platformSessionSupport,
        InternalRequestAuthenticator internalRequestAuthenticator,
        InternalRequestAuthenticationProperties properties,
        ObjectProvider<InternalServiceCompatibilityAuthenticationAdapter> compatibilityAuthenticationAdapterProvider
    ) {
        this.platformSessionSupport = platformSessionSupport;
        this.internalRequestAuthenticator = internalRequestAuthenticator;
        this.properties = properties;
        this.compatibilityAuthenticationAdapter = compatibilityAuthenticationAdapterProvider.getIfAvailable();
    }

    public SecurityContext resolveInternalContext(SecurityRequest request) {
        Map<String, String> attributes = new LinkedHashMap<>(request.attributes());
        InternalRequestAuthenticationResult result = authenticate(request);
        if (result.allowed()) {
            attributes.put("auth.authenticated", "true");
            attributes.put("auth.principal", INTERNAL_PRINCIPAL);
            return new SecurityContext(true, INTERNAL_PRINCIPAL, INTERNAL_AUTHORITIES, Map.copyOf(attributes));
        }
        return new SecurityContext(false, null, Set.of(), Map.copyOf(attributes));
    }

    private InternalRequestAuthenticationResult authenticate(SecurityRequest request) {
        return switch (properties.getMode()) {
            case DISABLED -> InternalRequestAuthenticationResult.allow("INTERNAL_AUTH_DISABLED");
            case JWT -> authenticateWithPlatform(request);
            case LEGACY_SECRET -> authenticateCompatibility(request);
            case HYBRID -> authenticateHybrid(request);
        };
    }

    private InternalRequestAuthenticationResult authenticateHybrid(SecurityRequest request) {
        InternalRequestAuthenticationResult jwtResult = authenticateWithPlatform(request);
        if (jwtResult.allowed()) {
            return jwtResult;
        }

        InternalRequestAuthenticationResult compatibilityResult = authenticateCompatibility(request);
        if (compatibilityResult.allowed()) {
            return compatibilityResult;
        }
        return compatibilityResult.reason() == null || compatibilityResult.reason().isBlank()
            ? jwtResult
            : compatibilityResult;
    }

    private InternalRequestAuthenticationResult authenticateWithPlatform(SecurityRequest request) {
        String accessToken = trimToNull(request.attributes().get("auth.accessToken"));
        if (accessToken == null) {
            return InternalRequestAuthenticationResult.deny("JWT_MISSING");
        }
        Optional<PlatformAuthenticatedPrincipal> principal = platformSessionSupport.authenticateAccessToken(accessToken);
        if (principal.isPresent()) {
            return InternalRequestAuthenticationResult.allow("JWT_VALID");
        }
        return internalRequestAuthenticator.authenticateAccessToken(accessToken);
    }

    private InternalRequestAuthenticationResult authenticateCompatibility(SecurityRequest request) {
        if (compatibilityAuthenticationAdapter == null) {
            return InternalRequestAuthenticationResult.deny("LEGACY_SECRET_NOT_CONFIGURED");
        }
        Optional<PlatformAuthenticatedPrincipal> principal = compatibilityAuthenticationAdapter.authenticate(request);
        if (principal.isPresent()) {
            return InternalRequestAuthenticationResult.allow("LEGACY_SECRET_MATCH");
        }
        return InternalRequestAuthenticationResult.deny("INVALID_LEGACY_SECRET");
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
