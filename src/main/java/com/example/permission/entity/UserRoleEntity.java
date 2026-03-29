package com.example.permission.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "user_roles",
        indexes = {
                @Index(name = "idx_user_roles_user_scope", columnList = "user_id, scope_type, scope_id")
        }
)
public class UserRoleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, length = 100)
    private String userId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "role_id", nullable = false)
    private RoleEntity role;

    @Column(name = "scope_type", nullable = false, length = 50)
    private String scopeType;

    @Column(name = "scope_id", nullable = false, length = 100)
    private String scopeId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    protected UserRoleEntity() {
    }

    public UserRoleEntity(String userId, RoleEntity role, String scopeType, String scopeId) {
        this.userId = userId;
        this.role = role;
        this.scopeType = scopeType;
        this.scopeId = scopeId;
    }

    @PrePersist
    void prePersist() {
        createdAt = LocalDateTime.now();
    }

    public String getUserId() {
        return userId;
    }

    public RoleEntity getRole() {
        return role;
    }
}
