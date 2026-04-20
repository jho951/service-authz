package com.authzservice.app.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InternalRequestAuthenticatorTest {

    private static final String SECRET = "test-internal-jwt-secret";

    @Test
    void allowValidJwt() throws Exception {
        InternalRequestAuthenticator authenticator = new InternalRequestAuthenticator(properties(), new ObjectMapper());

        InternalRequestAuthenticationResult result = authenticator.authenticate(Map.of(
                "Authorization", "Bearer " + token("api-gateway", "authz-service", Instant.now().plusSeconds(60).getEpochSecond())
        ));

        assertTrue(result.allowed());
    }

    @Test
    void denyJwtWithWrongAudience() throws Exception {
        InternalRequestAuthenticator authenticator = new InternalRequestAuthenticator(properties(), new ObjectMapper());

        InternalRequestAuthenticationResult result = authenticator.authenticate(Map.of(
                "Authorization", "Bearer " + token("api-gateway", "other-service", Instant.now().plusSeconds(60).getEpochSecond())
        ));

        assertFalse(result.allowed());
    }

    private InternalRequestAuthenticationProperties properties() {
        InternalRequestAuthenticationProperties properties = new InternalRequestAuthenticationProperties();
        properties.setMode(InternalRequestAuthenticationProperties.Mode.JWT);
        properties.getJwt().setSecret(SECRET);
        properties.getJwt().setIssuer("api-gateway");
        properties.getJwt().setAudience("authz-service");
        return properties;
    }

    private String token(String issuer, String audience, long exp) throws Exception {
        String header = base64Url("{\"alg\":\"HS256\",\"typ\":\"JWT\"}");
        String payload = base64Url("{\"iss\":\"" + issuer + "\",\"aud\":\"" + audience + "\",\"exp\":" + exp + "}");
        String unsignedToken = header + "." + payload;
        return unsignedToken + "." + sign(unsignedToken);
    }

    private String sign(String value) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        return Base64.getUrlEncoder().withoutPadding().encodeToString(mac.doFinal(value.getBytes(StandardCharsets.UTF_8)));
    }

    private String base64Url(String value) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }
}
