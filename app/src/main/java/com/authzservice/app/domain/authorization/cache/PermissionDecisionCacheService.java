package com.authzservice.app.domain.authorization.cache;

import com.authzservice.app.domain.authorization.model.PermissionDecisionResult;
import com.authzservice.app.domain.authorization.model.Decision;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PermissionDecisionCacheService {

    private final Map<String, LocalCacheEntry> localCache = new ConcurrentHashMap<>();
    private final StringRedisTemplate redisTemplate;
    private final String keyPrefix;
    private final Duration localTtl;
    private final Duration redisTtl;

    public PermissionDecisionCacheService(
            ObjectProvider<StringRedisTemplate> redisTemplateProvider,
            @Value("${permission.cache.prefix:perm:}") String keyPrefix,
            @Value("${permission.cache.local-ttl-seconds:5}") long localTtlSeconds,
            @Value("${permission.cache.redis-ttl-seconds:60}") long redisTtlSeconds
    ) {
        this.redisTemplate = redisTemplateProvider.getIfAvailable();
        this.keyPrefix = keyPrefix;
        this.localTtl = Duration.ofSeconds(localTtlSeconds);
        this.redisTtl = Duration.ofSeconds(redisTtlSeconds);
    }

    public Optional<PermissionDecisionResult> get(String rawKey) {
        String key = keyPrefix + rawKey;
        LocalCacheEntry localEntry = localCache.get(key);
        if (localEntry != null && !localEntry.isExpired()) {
            return Optional.of(localEntry.result());
        }

        if (redisTemplate == null) {
            localCache.remove(key);
            return Optional.empty();
        }

        try {
            String value = redisTemplate.opsForValue().get(key);
            if (value == null || value.isBlank()) {
                localCache.remove(key);
                return Optional.empty();
            }

            PermissionDecisionResult result = decode(value);
            localCache.put(key, new LocalCacheEntry(result, System.currentTimeMillis() + localTtl.toMillis()));
            return Optional.of(result);
        } catch (Exception ignored) {
            return Optional.empty();
        }
    }

    public void put(String rawKey, PermissionDecisionResult result) {
        String key = keyPrefix + rawKey;
        localCache.put(key, new LocalCacheEntry(result, System.currentTimeMillis() + localTtl.toMillis()));

        if (redisTemplate == null) {
            return;
        }

        try {
            redisTemplate.opsForValue().set(key, encode(result), redisTtl);
        } catch (Exception ignored) {
        }
    }

    public void evictAll() {
        localCache.clear();
        if (redisTemplate == null) {
            return;
        }

        try {
            Set<String> keys = redisTemplate.keys(keyPrefix + "*");
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
            }
        } catch (Exception ignored) {
        }
    }

    private String encode(PermissionDecisionResult result) {
        return result.decision().name() + "|" + result.reason();
    }

    private PermissionDecisionResult decode(String value) {
        String[] split = value.split("\\|", 2);
        Decision decision = Decision.valueOf(split[0]);
        String reason = split.length > 1 ? split[1] : "UNKNOWN";
        return new PermissionDecisionResult(decision, reason);
    }

    private record LocalCacheEntry(PermissionDecisionResult result, long expiresAtMillis) {
        private boolean isExpired() {
            return System.currentTimeMillis() > expiresAtMillis;
        }
    }
}
