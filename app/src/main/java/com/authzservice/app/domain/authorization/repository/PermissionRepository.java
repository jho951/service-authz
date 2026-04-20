package com.authzservice.app.domain.authorization.repository;

import com.authzservice.app.domain.authorization.entity.PermissionEntity;
import com.authzservice.app.domain.authorization.model.PermissionCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PermissionRepository extends JpaRepository<PermissionEntity, String> {
    Optional<PermissionEntity> findByCode(PermissionCode code);
}
