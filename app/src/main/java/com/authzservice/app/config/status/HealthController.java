package com.authzservice.app.config.status;

import com.authzservice.common.swagger.SwaggerTag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@Tag(name = SwaggerTag.HEALTH, description = "Service health and readiness APIs")
public class HealthController {
    private final JdbcTemplate jdbcTemplate;
    private final StringRedisTemplate redisTemplate;

    public HealthController(JdbcTemplate jdbcTemplate, ObjectProvider<StringRedisTemplate> redisTemplateProvider) {
        this.jdbcTemplate = jdbcTemplate;
        this.redisTemplate = redisTemplateProvider.getIfAvailable();
    }

    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Returns the process liveness status.")
    public Map<String, Object> health() {
        return Map.of("status", "UP");
    }

    @GetMapping("/ready")
    @Operation(summary = "Readiness check", description = "Returns readiness based on database and Redis connectivity.")
    public Map<String, Object> ready() {
        boolean dbUp = isDbUp();
        boolean redisUp = isRedisUp();
        boolean ready = dbUp && redisUp;

        return Map.of(
                "status", ready ? "UP" : "DOWN",
                "components", Map.of(
                        "db", dbUp ? "UP" : "DOWN",
                        "redis", redisUp ? "UP" : "DOWN"
                )
        );
    }

    private boolean isDbUp() {
        try {
            Integer result = jdbcTemplate.queryForObject("select 1", Integer.class);
            return result != null && result == 1;
        } catch (DataAccessException ex) {
            return false;
        }
    }

    private boolean isRedisUp() {
        if (redisTemplate == null) return false;
        try {
            String pong = redisTemplate.getConnectionFactory().getConnection().ping();
            return "PONG".equalsIgnoreCase(pong);
        } catch (Exception ex) {
            return false;
        }
    }
}
