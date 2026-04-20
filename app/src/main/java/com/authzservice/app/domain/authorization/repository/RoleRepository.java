package com.authzservice.app.domain.authorization.repository;

import com.authzservice.app.domain.authorization.entity.RoleEntity;
import com.authzservice.app.domain.authorization.model.RoleCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RoleRepository extends JpaRepository<RoleEntity, UUID> {
    Optional<RoleEntity> findByName(RoleCode name);

    List<RoleEntity> findByNameIn(Collection<RoleCode> names);
}
