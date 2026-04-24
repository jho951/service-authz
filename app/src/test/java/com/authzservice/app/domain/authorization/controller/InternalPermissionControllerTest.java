package com.authzservice.app.domain.authorization.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:permission;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.h2.console.enabled=false",
        "permission.internal-auth.mode=LEGACY_SECRET",
        "permission.internal-auth.legacy-secret=test-secret",
        "permission.seed.sample-user-enabled=true"
})
@AutoConfigureMockMvc
class InternalPermissionControllerTest {

    private static final String INTERNAL_SECRET = "test-secret";

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("성공_admin 사용자의 admin read 권한은 허용된다")
    void allowAdminReadForAdminUser() throws Exception {
        mockMvc.perform(post("/permissions/internal/admin/verify")
                        .header("X-Internal-Request-Secret", INTERNAL_SECRET)
                        .header("X-User-Id", "admin-seed")
                        .header("X-Original-Method", "GET")
                        .header("X-Original-Path", "/admin/blocks"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("성공_admin manage 경로는 ADMIN_MANAGE로 판정된다")
    void allowAdminManagePathForAdminUser() throws Exception {
        mockMvc.perform(post("/permissions/internal/admin/verify")
                        .header("X-Internal-Request-Secret", INTERNAL_SECRET)
                        .header("X-User-Id", "admin-seed")
                        .header("X-Original-Method", "POST")
                        .header("X-Original-Path", "/admin/manage/settings"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("실패_header role이 ADMIN이어도 DB 권한이 없으면 거부된다")
    void denyDeleteForMemberRole() throws Exception {
        mockMvc.perform(post("/permissions/internal/admin/verify")
                        .header("X-Internal-Request-Secret", INTERNAL_SECRET)
                        .header("X-User-Id", "member-user")
                        .header("X-User-Role", "ADMIN")
                        .header("X-Original-Method", "DELETE")
                        .header("X-Original-Path", "/admin/blocks/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("실패_내부 시크릿이 일치하지 않으면 403을 반환한다")
    void forbiddenWhenInternalSecretMismatch() throws Exception {
        mockMvc.perform(post("/permissions/internal/admin/verify")
                        .header("X-Internal-Request-Secret", "wrong-secret")
                        .header("X-User-Id", "admin-seed")
                        .header("X-Original-Method", "GET")
                        .header("X-Original-Path", "/admin/blocks"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("성공_잘못된 role 헤더는 권한 판단에서 무시한다")
    void ignoreInvalidUserRoleHeader() throws Exception {
        mockMvc.perform(post("/permissions/internal/admin/verify")
                        .header("X-Internal-Request-Secret", INTERNAL_SECRET)
                        .header("X-User-Id", "admin-seed")
                        .header("X-User-Role", "BAD_ROLE")
                        .header("X-Original-Method", "GET")
                        .header("X-Original-Path", "/admin/blocks"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("실패_필수 헤더 누락 시 400을 반환한다")
    void badRequestWhenMissingRequiredHeader() throws Exception {
        mockMvc.perform(post("/permissions/internal/admin/verify")
                        .header("X-Internal-Request-Secret", INTERNAL_SECRET)
                        .header("X-Original-Method", "GET")
                        .header("X-Original-Path", "/admin/blocks"))
                .andExpect(status().isBadRequest());
    }
}
