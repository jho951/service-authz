package com.authzservice.app.domain.authorization.service;

import com.authzservice.app.domain.authorization.dto.AdminPermissionVerifyRequest;
import com.authzservice.app.domain.authorization.model.PermissionDecisionResult;
import com.authzservice.app.domain.authorization.policy.PermissionPolicyCatalog;
import com.authzservice.app.domain.authorization.model.PermissionCode;
import com.authzservice.app.domain.authorization.model.RoleCode;
import com.authzservice.app.domain.authorization.repository.RolePermissionRepository;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class AuthzPolicyEngineService {

    private final RolePermissionRepository rolePermissionRepository;

    public AuthzPolicyEngineService(RolePermissionRepository rolePermissionRepository) {
        this.rolePermissionRepository = rolePermissionRepository;
    }

    public PermissionDecisionResult evaluate(PermissionCode requiredPermission,
                                             String userId,
                                             Set<RoleCode> effectiveRoles,
                                             AdminPermissionVerifyRequest request) {
        Set<PermissionCode> grantedPermissions = rolePermissionRepository.findPermissionCodesByRoleCodes(effectiveRoles);
        boolean allowed = grantedPermissions.contains(requiredPermission);
        if (allowed) {
            return PermissionDecisionResult.allow("permission granted for " + PermissionPolicyCatalog.policyKey(requiredPermission));
        }
        return PermissionDecisionResult.deny("permission missing for " + PermissionPolicyCatalog.policyKey(requiredPermission));
    }
}
