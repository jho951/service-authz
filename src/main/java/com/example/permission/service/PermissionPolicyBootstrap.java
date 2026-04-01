package com.example.permission.service;

import com.example.permission.domain.PermissionCode;
import com.example.permission.domain.RoleCode;
import com.example.permission.entity.PermissionEntity;
import com.example.permission.entity.RoleEntity;
import com.example.permission.entity.RolePermissionEntity;
import com.example.permission.entity.UserRoleEntity;
import com.example.permission.repository.PermissionRepository;
import com.example.permission.repository.RolePermissionRepository;
import com.example.permission.repository.RoleRepository;
import com.example.permission.repository.UserRoleRepository;
import com.pluginpolicyengine.core.store.InMemoryFlagStore;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class PermissionPolicyBootstrap implements ApplicationRunner {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final UserRoleRepository userRoleRepository;
    private final InMemoryFlagStore authzPolicyFlagStore;

    public PermissionPolicyBootstrap(RoleRepository roleRepository,
                                     PermissionRepository permissionRepository,
                                     RolePermissionRepository rolePermissionRepository,
                                     UserRoleRepository userRoleRepository,
                                     InMemoryFlagStore authzPolicyFlagStore) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.rolePermissionRepository = rolePermissionRepository;
        this.userRoleRepository = userRoleRepository;
        this.authzPolicyFlagStore = authzPolicyFlagStore;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        Map<RoleCode, RoleEntity> roles = ensureRoles();
        Map<PermissionCode, PermissionEntity> permissions = ensurePermissions();
        ensureRolePermissions(roles, permissions);
        ensureSampleUserRole(roles);
        seedPolicyEngine(permissions.keySet());
    }

    private Map<RoleCode, RoleEntity> ensureRoles() {
        Map<RoleCode, String> roleDescriptions = Map.of(
                RoleCode.ADMIN, "운영 관리자",
                RoleCode.MANAGER, "서비스 관리자",
                RoleCode.MEMBER, "일반 사용자"
        );

        Map<RoleCode, RoleEntity> roles = new HashMap<>();
        for (RoleCode roleCode : RoleCode.values()) {
            RoleEntity role = roleRepository.findByName(roleCode)
                    .orElseGet(() -> roleRepository.save(new RoleEntity(roleCode, roleDescriptions.get(roleCode))));
            roles.put(roleCode, role);
        }
        return roles;
    }

    private Map<PermissionCode, PermissionEntity> ensurePermissions() {
        Map<PermissionCode, String> descriptions = Map.of(
                PermissionCode.ADMIN_READ, "관리자 리소스 조회",
                PermissionCode.ADMIN_WRITE, "관리자 리소스 생성/수정",
                PermissionCode.ADMIN_DELETE, "관리자 리소스 삭제",
                PermissionCode.ADMIN_MANAGE, "관리자 설정 변경"
        );

        Map<PermissionCode, PermissionEntity> permissions = new HashMap<>();
        for (PermissionCode code : PermissionCode.values()) {
            PermissionEntity permission = permissionRepository.findByCode(code)
                    .orElseGet(() -> permissionRepository.save(new PermissionEntity(code, descriptions.get(code))));
            permissions.put(code, permission);
        }
        return permissions;
    }

    private void ensureRolePermissions(Map<RoleCode, RoleEntity> roles, Map<PermissionCode, PermissionEntity> permissions) {
        if (rolePermissionRepository.count() > 0) {
            return;
        }

        List<RolePermissionEntity> mappings = List.of(
                new RolePermissionEntity(roles.get(RoleCode.ADMIN), permissions.get(PermissionCode.ADMIN_READ)),
                new RolePermissionEntity(roles.get(RoleCode.ADMIN), permissions.get(PermissionCode.ADMIN_WRITE)),
                new RolePermissionEntity(roles.get(RoleCode.ADMIN), permissions.get(PermissionCode.ADMIN_DELETE)),
                new RolePermissionEntity(roles.get(RoleCode.ADMIN), permissions.get(PermissionCode.ADMIN_MANAGE)),

                new RolePermissionEntity(roles.get(RoleCode.MANAGER), permissions.get(PermissionCode.ADMIN_READ)),
                new RolePermissionEntity(roles.get(RoleCode.MANAGER), permissions.get(PermissionCode.ADMIN_WRITE)),

                new RolePermissionEntity(roles.get(RoleCode.MEMBER), permissions.get(PermissionCode.ADMIN_READ))
        );

        rolePermissionRepository.saveAll(mappings);
    }

    private void ensureSampleUserRole(Map<RoleCode, RoleEntity> roles) {
        if (!userRoleRepository.findAllByUserId("admin-seed").isEmpty()) {
            return;
        }

        userRoleRepository.save(new UserRoleEntity("admin-seed", roles.get(RoleCode.ADMIN), "GLOBAL", "*"));
    }

    private void seedPolicyEngine(Set<PermissionCode> permissionCodes) {
        for (PermissionCode permissionCode : permissionCodes) {
            authzPolicyFlagStore.put(PermissionPolicyCatalog.toFlagDefinition(permissionCode));
        }
    }
}
