package com.authzservice.app.domain.authorization;

import com.authzservice.app.domain.authorization.cache.PermissionDecisionCacheService;
import com.authzservice.app.domain.authorization.repository.RolePermissionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "permission.internal-auth.mode=LEGACY_SECRET",
        "permission.internal-auth.legacy-secret=test-secret",
        "permission.seed.sample-user-enabled=true"
})
@AutoConfigureMockMvc
class RolePermissionDecisionIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RolePermissionRepository rolePermissionRepository;

    @Autowired
    private PermissionDecisionCacheService cacheService;

    @Test
    @DirtiesContext
    @DisplayName("실패_role_permissions 매핑이 없으면 ADMIN role 사용자도 거부된다")
    void denyWhenRolePermissionMappingIsMissing() throws Exception {
        rolePermissionRepository.deleteAll();
        cacheService.evictAll();

        mockMvc.perform(post("/permissions/internal/admin/verify")
                        .header("X-Internal-Request-Secret", "test-secret")
                        .header("X-User-Id", "admin-seed")
                        .header("X-Original-Method", "GET")
                        .header("X-Original-Path", "/admin/blocks"))
                .andExpect(status().isForbidden());
    }
}
