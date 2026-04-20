package com.authzservice.app.domain.authorization.policy;

import com.authzservice.app.domain.authorization.model.PermissionCode;
import com.authzservice.app.domain.authorization.model.RoleCode;
import com.authzservice.app.domain.authorization.entity.PermissionEntity;
import com.authzservice.app.domain.authorization.entity.RoleEntity;
import com.authzservice.app.domain.authorization.entity.RolePermissionEntity;
import com.authzservice.app.domain.authorization.entity.UserRoleEntity;
import com.authzservice.app.domain.authorization.repository.PermissionRepository;
import com.authzservice.app.domain.authorization.repository.RolePermissionRepository;
import com.authzservice.app.domain.authorization.repository.RoleRepository;
import com.authzservice.app.domain.authorization.repository.UserRoleRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class PermissionPolicyBootstrap implements ApplicationRunner {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final UserRoleRepository userRoleRepository;
    private final boolean sampleUserEnabled;

    public PermissionPolicyBootstrap(RoleRepository roleRepository,
                                     PermissionRepository permissionRepository,
                                     RolePermissionRepository rolePermissionRepository,
                                     UserRoleRepository userRoleRepository,
                                     @Value("${permission.seed.sample-user-enabled:false}") boolean sampleUserEnabled) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.rolePermissionRepository = rolePermissionRepository;
        this.userRoleRepository = userRoleRepository;
        this.sampleUserEnabled = sampleUserEnabled;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        Map<RoleCode, RoleEntity> roles = ensureRoles();
        Map<PermissionCode, PermissionEntity> permissions = ensurePermissions();
        ensureRolePermissions(roles, permissions);
        if (sampleUserEnabled) {
            ensureSampleUserRole(roles);
        }
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

}
