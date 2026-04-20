package com.authzservice.app.domain.authorization.repository;

import com.authzservice.app.domain.authorization.entity.PermissionEntity;
import com.authzservice.app.domain.authorization.model.PermissionCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PermissionRepository extends JpaRepository<PermissionEntity, UUID> {
    Optional<PermissionEntity> findByCode(PermissionCode code);
}
