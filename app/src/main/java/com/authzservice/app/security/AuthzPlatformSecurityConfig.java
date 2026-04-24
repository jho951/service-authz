package com.authzservice.app.security;

import com.authzservice.app.domain.authorization.support.PermissionHeaderNames;
import io.github.jho951.platform.security.api.SecurityContext;
import io.github.jho951.platform.security.api.SecurityContextResolver;
import io.github.jho951.platform.security.auth.PlatformAuthenticatedPrincipal;
import io.github.jho951.platform.security.auth.PlatformSessionSupportFactory;
import io.github.jho951.platform.security.web.SecurityFailureResponse;
import io.github.jho951.platform.security.web.SecurityFailureResponseWriter;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class AuthzPlatformSecurityConfig {

    private static final String INTERNAL_PRINCIPAL = "authz-internal-caller";
    private static final Set<String> INTERNAL_AUTHORITIES = Set.of("ROLE_INTERNAL");

    @Bean
    public PlatformSessionSupportFactory authzPlatformSessionSupportFactory(
            InternalRequestAuthenticator internalRequestAuthenticator
    ) {
        return () -> (accessToken, sessionId) -> authenticateInternalAccessToken(accessToken, internalRequestAuthenticator);
    }

    @Bean
    public SecurityContextResolver authzSecurityContextResolver(
            AuthzInternalRequestAuthorizer authorizer
    ) {
        return request -> {
            if (isInternalPermissionPath(request.path())) {
                return authorizer.resolveInternalContext(request);
            }
            return anonymousContext(request.attributes());
        };
    }

    @Bean
    public SecurityFailureResponseWriter authzSecurityFailureResponseWriter() {
        SecurityFailureResponseWriter delegate = SecurityFailureResponseWriter.json();
        return (request, response, failure) -> {
            if (isInternalPermissionPath(request.getRequestURI())
                    && failure.status() == 401
                    && hasText(request.getHeader(PermissionHeaderNames.INTERNAL_REQUEST_SECRET))) {
                delegate.write(request, response, new SecurityFailureResponse(403, "security.denied", failure.message()));
                return;
            }
            delegate.write(request, response, failure);
        };
    }

    @Bean
    public SecurityFilterChain authzSecurityFilterChain(
            HttpSecurity http,
            @Qualifier("securityServletFilter") jakarta.servlet.Filter platformSecurityServletFilter
    ) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/health",
                                "/ready",
                                "/actuator/prometheus",
                                "/actuator/health",
                                "/actuator/health/**",
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/favicon.ico",
                                "/error"
                        ).permitAll()
                        .requestMatchers("/permissions/internal/admin/verify").permitAll()
                        .anyRequest().denyAll()
                );

        http.addFilterBefore(platformSecurityServletFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    private static boolean isInternalPermissionPath(String path) {
        return path != null && (path.equals("/permissions/internal") || path.startsWith("/permissions/internal/"));
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private static Optional<PlatformAuthenticatedPrincipal> authenticateInternalAccessToken(
            String accessToken,
            InternalRequestAuthenticator internalRequestAuthenticator
    ) {
        if (accessToken == null || accessToken.isBlank()) {
            return Optional.empty();
        }
        InternalRequestAuthenticationResult result = internalRequestAuthenticator.authenticateAccessToken(accessToken);
        if (!result.allowed()) {
            return Optional.empty();
        }
        return Optional.of(new PlatformAuthenticatedPrincipal(INTERNAL_PRINCIPAL, INTERNAL_AUTHORITIES, Map.of()));
    }

    private static SecurityContext anonymousContext(Map<String, String> attributes) {
        return new SecurityContext(false, null, Set.of(), attributes);
    }
}
