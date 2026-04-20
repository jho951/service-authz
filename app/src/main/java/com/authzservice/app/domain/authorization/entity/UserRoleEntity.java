package com.authzservice.app.domain.authorization.entity;

import com.authzservice.common.base.entity.BaseEntity;
import jakarta.persistence.*;

@Entity
@Table(
        name = "user_roles",
        indexes = {
                @Index(name = "idx_user_roles_user_scope", columnList = "user_id, scope_type, scope_id")
        }
)
public class UserRoleEntity extends BaseEntity {

    @Column(name = "user_id", nullable = false, length = 100)
    private String userId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "role_id", nullable = false)
    private RoleEntity role;

    @Column(name = "scope_type", nullable = false, length = 50)
    private String scopeType;

    @Column(name = "scope_id", nullable = false, length = 100)
    private String scopeId;

    protected UserRoleEntity() {
    }

    public UserRoleEntity(String userId, RoleEntity role, String scopeType, String scopeId) {
        this.userId = userId;
        this.role = role;
        this.scopeType = scopeType;
        this.scopeId = scopeId;
    }

    public String getUserId() {
        return userId;
    }

    public RoleEntity getRole() {
        return role;
    }
}
