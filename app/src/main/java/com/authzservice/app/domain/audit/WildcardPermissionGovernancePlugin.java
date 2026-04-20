package com.authzservice.app.domain.audit;

import io.github.jho951.platform.governance.api.GovernanceContext;
import io.github.jho951.platform.governance.api.GovernancePolicyPlugin;
import io.github.jho951.platform.governance.api.GovernanceRequest;
import io.github.jho951.platform.governance.api.GovernanceVerdict;
import org.springframework.stereotype.Component;

@Component
public class WildcardPermissionGovernancePlugin implements GovernancePolicyPlugin {

    @Override
    public String name() {
        return "authz-wildcard-permission-policy";
    }

    @Override
    public boolean supports(GovernanceRequest request, GovernanceContext context) {
        return "authz.permission".equals(request.resource())
                && "grant".equals(request.action());
    }

    @Override
    public GovernanceVerdict evaluate(GovernanceRequest request, GovernanceContext context) {
        String permission = request.attributes().get("permission");

        if ("*".equals(permission) || (permission != null && permission.endsWith(":*"))) {
            return GovernanceVerdict.deny(name(), "와일드카드 권한 부여는 차단됩니다.");
        }

        return GovernanceVerdict.allow(name(), "권한 부여 governance 정책을 통과했습니다.");
    }
}
