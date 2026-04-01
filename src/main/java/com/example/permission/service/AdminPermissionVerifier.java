package com.example.permission.service;

import com.example.permission.api.AdminPermissionVerifyRequest;
import com.example.permission.api.PermissionBadRequestException;
import com.example.permission.domain.PermissionCode;
import com.example.permission.domain.RoleCode;
import com.example.permission.repository.UserRoleRepository;
import org.springframework.stereotype.Service;

import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AdminPermissionVerifier {

    private final UserRoleRepository userRoleRepository;
    private final PermissionDecisionCacheService cacheService;
    private final AuthzPolicyEngineService policyEngineService;

    public AdminPermissionVerifier(UserRoleRepository userRoleRepository,
                                   PermissionDecisionCacheService cacheService,
                                   AuthzPolicyEngineService policyEngineService) {
        this.userRoleRepository = userRoleRepository;
        this.cacheService = cacheService;
        this.policyEngineService = policyEngineService;
    }

    public PermissionDecisionResult verify(AdminPermissionVerifyRequest request) {
        if (!isAdminPath(request.originalPath())) {
            return PermissionDecisionResult.deny("NOT_ADMIN_PATH");
        }

        PermissionCode requiredPermission = resolvePermissionCode(request.originalMethod(), request.originalPath());
        Set<RoleCode> effectiveRoles = resolveRoles(request.userId(), request.userRole());
        if (effectiveRoles.isEmpty()) {
            return PermissionDecisionResult.deny("NO_ROLE");
        }
        String cacheKey = cacheKey(request, requiredPermission, effectiveRoles);

        return cacheService.get(cacheKey)
                .orElseGet(() -> {
                    PermissionDecisionResult computed = computeDecision(request, requiredPermission, effectiveRoles);
                    cacheService.put(cacheKey, computed);
                    return computed;
                });
    }

    private PermissionDecisionResult computeDecision(AdminPermissionVerifyRequest request,
                                                     PermissionCode requiredPermission,
                                                     Set<RoleCode> effectiveRoles) {
        PermissionDecisionResult result = policyEngineService.evaluate(requiredPermission, request.userId(), effectiveRoles, request);
        if (!result.isAllowed()) {
            return PermissionDecisionResult.deny("POLICY_DENY:" + result.reason());
        }
        return PermissionDecisionResult.allow("POLICY_MATCH:" + result.reason());
    }

    private Set<RoleCode> resolveRoles(String userId, String roleHeaderValue) {
        EnumSet<RoleCode> roleCodes = EnumSet.noneOf(RoleCode.class);

        roleCodes.addAll(userRoleRepository.findRoleCodesByUserId(userId));

        if (roleHeaderValue != null && !roleHeaderValue.isBlank()) {
            try {
                roleCodes.add(RoleCode.valueOf(roleHeaderValue.trim().toUpperCase()));
            } catch (IllegalArgumentException ex) {
                throw new PermissionBadRequestException("X-User-Role 값이 올바르지 않습니다.");
            }
        }
        return roleCodes;
    }

    private String cacheKey(AdminPermissionVerifyRequest request, PermissionCode permissionCode, Set<RoleCode> effectiveRoles) {
        String roleSnapshot = effectiveRoles.stream()
                .map(RoleCode::name)
                .sorted()
                .collect(Collectors.joining(","));
        String headerRole = request.userRole() == null ? "-" : request.userRole().trim().toUpperCase();
        return request.userId() + ":" + request.originalMethod() + ":" + request.originalPath() + ":" + permissionCode.name() + ":" + headerRole + ":" + roleSnapshot;
    }

    private PermissionCode resolvePermissionCode(String method, String path) {
        if (path.startsWith("/v1/admin/manage") || path.startsWith("/admin/manage")) {
            return PermissionCode.ADMIN_MANAGE;
        }

        return switch (method) {
            case "GET", "HEAD", "OPTIONS" -> PermissionCode.ADMIN_READ;
            case "POST", "PUT", "PATCH" -> PermissionCode.ADMIN_WRITE;
            case "DELETE" -> PermissionCode.ADMIN_DELETE;
            default -> throw new PermissionBadRequestException("지원하지 않는 HTTP Method입니다.");
        };
    }

    private boolean isAdminPath(String path) {
        return path.equals("/v1/admin")
                || path.startsWith("/v1/admin/")
                || path.equals("/admin")
                || path.startsWith("/admin/");
    }
}
