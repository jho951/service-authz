package com.example.permission.repository;

import com.example.permission.domain.PermissionCode;
import com.example.permission.entity.PermissionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PermissionRepository extends JpaRepository<PermissionEntity, Long> {
    Optional<PermissionEntity> findByCode(PermissionCode code);
}
