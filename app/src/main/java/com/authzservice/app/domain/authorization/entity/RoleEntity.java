package com.authzservice.app.domain.authorization.entity;

import com.authzservice.common.base.entity.BaseEntity;
import com.authzservice.app.domain.authorization.model.RoleCode;
import jakarta.persistence.*;

@Entity
@Table(name = "roles", uniqueConstraints = @UniqueConstraint(name = "uk_roles_name", columnNames = "name"))
public class RoleEntity extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "name", nullable = false, length = 50)
    private RoleCode name;

    @Column(name = "description", nullable = false, length = 255)
    private String description;

    protected RoleEntity() {
    }

    public RoleEntity(RoleCode name, String description) {
        this.name = name;
        this.description = description;
    }

    public RoleCode getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}
