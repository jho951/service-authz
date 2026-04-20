package com.authzservice.app.domain.authorization.cache;

import com.authzservice.app.domain.authorization.model.PermissionDecisionResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.core.StringRedisTemplate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PermissionDecisionCacheServiceTest {

    @Test
    @DisplayName("Redis 장애가 있어도 캐시 조회와 저장은 예외를 밖으로 던지지 않는다")
    @SuppressWarnings("unchecked")
    void redisFailureDoesNotBreakGetOrPut() {
        ObjectProvider<StringRedisTemplate> provider = mock(ObjectProvider.class);
        StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
        when(provider.getIfAvailable()).thenReturn(redisTemplate);
        when(redisTemplate.opsForValue()).thenThrow(new RuntimeException("redis down"));

        PermissionDecisionCacheService cacheService = new PermissionDecisionCacheService(provider, "perm:", 5, 60);

        assertTrue(cacheService.get("admin-seed:GET:/admin/blocks").isEmpty());
        assertDoesNotThrow(() -> cacheService.put(
                "admin-seed:GET:/admin/blocks",
                PermissionDecisionResult.allow("POLICY_MATCH:ALLOW")
        ));
    }
}
