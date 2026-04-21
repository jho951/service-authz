package com.authzservice.app.config;

import com.auditlog.api.AuditSink;
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
    public AuditSink authzGovernanceAuditSink() {
        return event -> log.info(
            "governanceAudit eventId={} action={} resourceType={} resourceId={} result={} reason={} requestId={} traceId={}",
            event.getEventId(),
            event.getAction(),
            event.getResourceType(),
            event.getResourceId(),
            event.getResult(),
            event.getReason(),
            event.getRequestId(),
            event.getTraceId()
        );
    }
}
