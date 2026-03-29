package com.example.permission.repository;

import com.example.permission.domain.RoleCode;
import com.example.permission.entity.RolePermissionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Set;

public interface RolePermissionRepository extends JpaRepository<RolePermissionEntity, Long> {

    @Query("""
            select p.code
            from RolePermissionEntity rp
            join rp.role r
            join rp.permission p
            where r.name in :roleCodes
            """)
    Set<com.example.permission.domain.PermissionCode> findPermissionCodesByRoleCodes(@Param("roleCodes") Set<RoleCode> roleCodes);
}
