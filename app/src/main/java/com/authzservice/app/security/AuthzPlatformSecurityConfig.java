package com.authzservice.app.security;

import io.github.jho951.platform.security.api.SecurityContext;
import io.github.jho951.platform.security.api.SecurityContextResolver;
import jakarta.servlet.Filter;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.ObjectProvider;
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

    @Bean
    public SecurityContextResolver authzSecurityContextResolver() {
        return request -> {
            if (isInternalPermissionPath(request.path())) {
                return new SecurityContext(true, INTERNAL_PRINCIPAL, Set.of("ROLE_INTERNAL"), Map.of());
            }
            return new SecurityContext(false, null, Set.of(), Map.of());
        };
    }

    @Bean
    public SecurityFilterChain authzSecurityFilterChain(
            HttpSecurity http,
            @Qualifier("securityServletFilter") ObjectProvider<Filter> platformSecurityServletFilterProvider
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

        Filter platformSecurityServletFilter = platformSecurityServletFilterProvider.getIfAvailable();
        if (platformSecurityServletFilter != null) {
            http.addFilterBefore(platformSecurityServletFilter, UsernamePasswordAuthenticationFilter.class);
        }

        return http.build();
    }

    private static boolean isInternalPermissionPath(String path) {
        return path != null && (path.equals("/permissions/internal") || path.startsWith("/permissions/internal/"));
    }
}
