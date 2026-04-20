package com.authzservice.app.domain.authorization.policy;

import com.authzservice.app.domain.authorization.model.PermissionCode;
import java.util.Locale;
import java.util.Optional;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;

@Component
public class PermissionRoutePolicyResolver {

    private final PermissionRoutePolicyProperties properties;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public PermissionRoutePolicyResolver(PermissionRoutePolicyProperties properties) {
        this.properties = properties;
    }

    public Optional<PermissionCode> resolve(String method, String path) {
        String normalizedMethod = method.toUpperCase(Locale.ROOT);
        return properties.getRoutes().stream()
                .filter(route -> matchesMethod(route, normalizedMethod))
                .filter(route -> matchesPath(route, path))
                .map(PermissionRoutePolicyProperties.Route::permission)
                .findFirst();
    }

    private boolean matchesMethod(PermissionRoutePolicyProperties.Route route, String method) {
        return route.methods() == null
                || route.methods().isEmpty()
                || route.methods().stream().map(value -> value.toUpperCase(Locale.ROOT)).anyMatch(method::equals);
    }

    private boolean matchesPath(PermissionRoutePolicyProperties.Route route, String path) {
        if (route.patterns() == null || route.patterns().isEmpty()) {
            return false;
        }
        return route.patterns().stream().anyMatch(pattern -> pathMatcher.match(pattern, path));
    }
}
