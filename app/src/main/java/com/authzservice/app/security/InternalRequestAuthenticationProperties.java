package com.authzservice.app.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "permission.internal-auth")
public class InternalRequestAuthenticationProperties {

    private Mode mode = Mode.HYBRID;
    private String legacySecret = "";
    private Jwt jwt = new Jwt();

    public Mode getMode() {
        return mode;
    }

    public void setMode(Mode mode) {
        this.mode = mode == null ? Mode.HYBRID : mode;
    }

    public String getLegacySecret() {
        return legacySecret;
    }

    public void setLegacySecret(String legacySecret) {
        this.legacySecret = legacySecret == null ? "" : legacySecret;
    }

    public Jwt getJwt() {
        return jwt;
    }

    public void setJwt(Jwt jwt) {
        this.jwt = jwt == null ? new Jwt() : jwt;
    }

    public enum Mode {
        DISABLED,
        LEGACY_SECRET,
        JWT,
        HYBRID
    }

    public static class Jwt {
        private String secret = "";
        private String issuer = "api-gateway";
        private String audience = "authz-service";
        private long clockSkewSeconds = 30;

        public String getSecret() {
            return secret;
        }

        public void setSecret(String secret) {
            this.secret = secret == null ? "" : secret;
        }

        public String getIssuer() {
            return issuer;
        }

        public void setIssuer(String issuer) {
            this.issuer = issuer == null ? "" : issuer;
        }

        public String getAudience() {
            return audience;
        }

        public void setAudience(String audience) {
            this.audience = audience == null ? "" : audience;
        }

        public long getClockSkewSeconds() {
            return clockSkewSeconds;
        }

        public void setClockSkewSeconds(long clockSkewSeconds) {
            this.clockSkewSeconds = clockSkewSeconds;
        }
    }
}
