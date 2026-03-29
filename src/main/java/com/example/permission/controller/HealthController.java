package com.example.permission.controller;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class HealthController {
    private final JdbcTemplate jdbcTemplate;
    private final StringRedisTemplate redisTemplate;

    public HealthController(JdbcTemplate jdbcTemplate, ObjectProvider<StringRedisTemplate> redisTemplateProvider) {
        this.jdbcTemplate = jdbcTemplate;
        this.redisTemplate = redisTemplateProvider.getIfAvailable();
    }

    @GetMapping("/health")
    public Map<String, Object> health() {
        return Map.of("status", "UP");
    }

    @GetMapping("/ready")
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
