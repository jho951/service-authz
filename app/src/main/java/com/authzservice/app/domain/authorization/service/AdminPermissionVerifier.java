package com.authzservice.app.domain.authorization.service;

import com.authzservice.app.domain.authorization.dto.AdminPermissionVerifyRequest;
import com.authzservice.app.domain.authorization.cache.AuthorizationPolicyVersionService;
import com.authzservice.app.domain.authorization.cache.PermissionDecisionCacheService;
import com.authzservice.app.domain.authorization.model.PermissionDecisionResult;
import com.authzservice.app.domain.authorization.policy.PermissionRoutePolicyResolver;
import com.authzservice.app.domain.authorization.model.PermissionCode;
import com.authzservice.app.domain.authorization.model.RoleCode;
import com.authzservice.app.domain.authorization.repository.UserRoleRepository;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AdminPermissionVerifier {

    private final UserRoleRepository userRoleRepository;
    private final PermissionDecisionCacheService cacheService;
    private final AuthorizationPolicyVersionService policyVersionService;
    private final PermissionRoutePolicyResolver routePolicyResolver;
    private final AuthzPolicyEngineService policyEngineService;

    public AdminPermissionVerifier(UserRoleRepository userRoleRepository,
                                   PermissionDecisionCacheService cacheService,
                                   AuthorizationPolicyVersionService policyVersionService,
                                   PermissionRoutePolicyResolver routePolicyResolver,
                                   AuthzPolicyEngineService policyEngineService) {
        this.userRoleRepository = userRoleRepository;
        this.cacheService = cacheService;
        this.policyVersionService = policyVersionService;
        this.routePolicyResolver = routePolicyResolver;
        this.policyEngineService = policyEngineService;
    }

    public PermissionDecisionResult verify(AdminPermissionVerifyRequest request) {
        PermissionCode requiredPermission = routePolicyResolver
                .resolve(request.originalMethod(), request.originalPath())
                .orElse(null);
        if (requiredPermission == null) {
            return PermissionDecisionResult.deny("NO_ROUTE_POLICY");
        }
        Set<RoleCode> effectiveRoles = resolveRoles(request.userId());
        if (effectiveRoles.isEmpty()) {
            return PermissionDecisionResult.deny("NO_ROLE");
        }
        String cacheKey = cacheKey(request, requiredPermission, effectiveRoles, policyVersionService.currentVersion());

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

    private Set<RoleCode> resolveRoles(String userId) {
        return userRoleRepository.findRoleCodesByUserId(userId);
    }

    private String cacheKey(AdminPermissionVerifyRequest request,
                            PermissionCode permissionCode,
                            Set<RoleCode> effectiveRoles,
                            String policyVersion) {
        String roleSnapshot = effectiveRoles.stream()
                .map(RoleCode::name)
                .sorted()
                .collect(Collectors.joining(","));
        return policyVersion + ":" + request.userId() + ":" + request.originalMethod() + ":" + request.originalPath() + ":" + permissionCode.name() + ":" + roleSnapshot;
    }
}
