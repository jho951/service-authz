package com.example.permission.service;

import com.example.permission.api.AdminPermissionVerifyRequest;
import com.example.permission.domain.PermissionCode;
import com.example.permission.domain.RoleCode;
import com.pluginpolicyengine.core.FeatureFlagService;
import com.pluginpolicyengine.core.FlagContext;
import com.pluginpolicyengine.core.FlagDecision;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class AuthzPolicyEngineService {

    private final FeatureFlagService featureFlagService;

    public AuthzPolicyEngineService(FeatureFlagService featureFlagService) {
        this.featureFlagService = featureFlagService;
    }

    public PermissionDecisionResult evaluate(PermissionCode requiredPermission,
                                             String userId,
                                             Set<RoleCode> effectiveRoles,
                                             AdminPermissionVerifyRequest request) {
        FlagContext context = FlagContext.builder()
                .userId(userId)
                .groups(effectiveRoles.stream().map(RoleCode::name).toList())
                .attr("originalMethod", request.originalMethod())
                .attr("originalPath", request.originalPath())
                .attr("permission", requiredPermission.name())
                .build();

        FlagDecision decision = featureFlagService.evaluate(PermissionPolicyCatalog.policyKey(requiredPermission), context);
        return decision.enabled()
                ? PermissionDecisionResult.allow(decision.reason())
                : PermissionDecisionResult.deny(decision.reason());
    }
}
