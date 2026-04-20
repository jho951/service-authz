package com.authzservice.app.domain.authorization.policy;

import com.authzservice.app.domain.authorization.model.PermissionCode;
import java.util.Locale;

public final class PermissionPolicyCatalog {

    private PermissionPolicyCatalog() {
    }

    public static String policyKey(PermissionCode code) {
        return "authz:" + code.name().toLowerCase(Locale.ROOT).replace('_', ':');
    }
}
