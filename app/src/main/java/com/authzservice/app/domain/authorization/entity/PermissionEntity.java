package com.authzservice.app.domain.authorization.entity;

import com.authzservice.common.base.entity.BaseEntity;
import com.authzservice.app.domain.authorization.model.PermissionCode;
import jakarta.persistence.*;

@Entity
@Table(name = "permissions", uniqueConstraints = @UniqueConstraint(name = "uk_permissions_code", columnNames = "code"))
public class PermissionEntity extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "code", nullable = false, length = 50)
    private PermissionCode code;

    @Column(name = "description", nullable = false, length = 255)
    private String description;

    protected PermissionEntity() {
    }

    public PermissionEntity(PermissionCode code, String description) {
        this.code = code;
        this.description = description;
    }

    public PermissionCode getCode() {
        return code;
    }
}
