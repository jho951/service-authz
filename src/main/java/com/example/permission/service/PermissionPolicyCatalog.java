package com.example.permission.service;

import com.example.permission.domain.PermissionCode;
import com.example.permission.domain.RoleCode;
import com.pluginpolicyengine.core.FlagDefinition;
import com.pluginpolicyengine.core.Targeting;

import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;

public final class PermissionPolicyCatalog {

    private PermissionPolicyCatalog() {
    }

    public static String policyKey(PermissionCode code) {
        return "authz:" + code.name().toLowerCase(Locale.ROOT).replace('_', ':');
    }

    public static Set<RoleCode> allowedRoles(PermissionCode code) {
        return switch (code) {
            case ADMIN_READ -> EnumSet.of(RoleCode.ADMIN, RoleCode.MANAGER, RoleCode.MEMBER);
            case ADMIN_WRITE -> EnumSet.of(RoleCode.ADMIN, RoleCode.MANAGER);
            case ADMIN_DELETE, ADMIN_MANAGE -> EnumSet.of(RoleCode.ADMIN);
        };
    }

    public static FlagDefinition toFlagDefinition(PermissionCode code) {
        Targeting.Builder targeting = Targeting.builder();
        for (RoleCode roleCode : allowedRoles(code)) {
            targeting.allowGroup(roleCode.name());
        }

        return FlagDefinition.builder(policyKey(code))
                .enabled(true)
                .targeting(targeting.build())
                .defaultVariant("allow")
                .build();
    }
}
