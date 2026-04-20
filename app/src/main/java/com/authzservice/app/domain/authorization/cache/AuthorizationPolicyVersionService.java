package com.authzservice.app.domain.authorization.cache;

import com.authzservice.app.domain.authorization.repository.RolePermissionRepository;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import org.springframework.stereotype.Service;

@Service
public class AuthorizationPolicyVersionService {

    private final RolePermissionRepository rolePermissionRepository;

    public AuthorizationPolicyVersionService(RolePermissionRepository rolePermissionRepository) {
        this.rolePermissionRepository = rolePermissionRepository;
    }

    public String currentVersion() {
        long mappingCount = rolePermissionRepository.count();
        long latestCreatedAt = rolePermissionRepository.findLatestCreatedAt()
                .map(this::toEpochMillis)
                .orElse(0L);
        return mappingCount + ":" + latestCreatedAt;
    }

    private long toEpochMillis(LocalDateTime value) {
        return value.toInstant(ZoneOffset.UTC).toEpochMilli();
    }
}
