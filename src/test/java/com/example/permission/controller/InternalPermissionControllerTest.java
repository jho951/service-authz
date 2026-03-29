package com.example.permission.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class InternalPermissionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("성공_admin 사용자의 admin read 권한은 허용된다")
    void allowAdminReadForAdminUser() throws Exception {
        mockMvc.perform(post("/permissions/internal/admin/verify")
                        .header("X-User-Id", "admin-seed")
                        .header("X-Original-Method", "GET")
                        .header("X-Original-Path", "/admin/blocks"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("실패_member 역할은 admin delete 권한이 없어 거부된다")
    void denyDeleteForMemberRole() throws Exception {
        mockMvc.perform(post("/permissions/internal/admin/verify")
                        .header("X-User-Id", "member-user")
                        .header("X-User-Role", "MEMBER")
                        .header("X-Original-Method", "DELETE")
                        .header("X-Original-Path", "/admin/blocks/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("실패_필수 헤더 누락 시 400을 반환한다")
    void badRequestWhenMissingRequiredHeader() throws Exception {
        mockMvc.perform(post("/permissions/internal/admin/verify")
                        .header("X-Original-Method", "GET")
                        .header("X-Original-Path", "/admin/blocks"))
                .andExpect(status().isBadRequest());
    }
}
