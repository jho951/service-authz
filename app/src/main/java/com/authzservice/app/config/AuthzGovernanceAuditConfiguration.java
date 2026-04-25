package com.authzservice.app.config;

import io.github.jho951.platform.governance.api.GovernanceAuditSink;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile({"prod", "production", "live"})
public class AuthzGovernanceAuditConfiguration {

    private static final Logger log = LoggerFactory.getLogger(AuthzGovernanceAuditConfiguration.class);

    @Bean
    public GovernanceAuditSink authzGovernanceAuditSink() {
        return entry -> log.info(
            "governanceAudit category={} message={} requestId={} traceId={}",
            entry.category(),
            entry.message(),
            entry.attributes().getOrDefault("requestId", ""),
            entry.attributes().getOrDefault("traceId", "")
        );
    }
}
