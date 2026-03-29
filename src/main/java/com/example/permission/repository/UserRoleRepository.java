package com.example.permission.repository;

import com.example.permission.entity.UserRoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

public interface UserRoleRepository extends JpaRepository<UserRoleEntity, Long> {
    List<UserRoleEntity> findAllByUserId(String userId);

    @Query("select ur.role.name from UserRoleEntity ur where ur.userId = :userId")
    Set<com.example.permission.domain.RoleCode> findRoleCodesByUserId(@Param("userId") String userId);
}
