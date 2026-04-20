package com.authzservice.app.domain.authorization.repository;

import com.authzservice.app.domain.authorization.entity.UserRoleEntity;
import com.authzservice.app.domain.authorization.model.RoleCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface UserRoleRepository extends JpaRepository<UserRoleEntity, UUID> {
    List<UserRoleEntity> findAllByUserId(String userId);

    @Query("select ur.role.name from UserRoleEntity ur where ur.userId = :userId")
    Set<RoleCode> findRoleCodesByUserId(@Param("userId") String userId);
}
