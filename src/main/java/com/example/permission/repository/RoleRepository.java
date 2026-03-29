package com.example.permission.repository;

import com.example.permission.domain.RoleCode;
import com.example.permission.entity.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface RoleRepository extends JpaRepository<RoleEntity, Long> {
    Optional<RoleEntity> findByName(RoleCode name);

    List<RoleEntity> findByNameIn(Collection<RoleCode> names);
}
