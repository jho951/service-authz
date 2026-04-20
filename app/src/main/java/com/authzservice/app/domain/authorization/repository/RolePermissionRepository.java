package com.authzservice.app.domain.authorization.repository;

import com.authzservice.app.domain.authorization.entity.RolePermissionEntity;
import com.authzservice.app.domain.authorization.model.PermissionCode;
import com.authzservice.app.domain.authorization.model.RoleCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

public interface RolePermissionRepository extends JpaRepository<RolePermissionEntity, String> {

    @Query("""
            select p.code
            from RolePermissionEntity rp
            join rp.role r
            join rp.permission p
            where r.name in :roleCodes
            """)
    Set<PermissionCode> findPermissionCodesByRoleCodes(@Param("roleCodes") Set<RoleCode> roleCodes);

    @Query("select max(rp.createdAt) from RolePermissionEntity rp")
    Optional<LocalDateTime> findLatestCreatedAt();
}
