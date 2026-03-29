package com.example.permission.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "role_permissions", uniqueConstraints = @UniqueConstraint(name = "uk_role_permissions", columnNames = {"role_id", "permission_id"}))
public class RolePermissionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "role_id", nullable = false)
    private RoleEntity role;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "permission_id", nullable = false)
    private PermissionEntity permission;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    protected RolePermissionEntity() {
    }

    public RolePermissionEntity(RoleEntity role, PermissionEntity permission) {
        this.role = role;
        this.permission = permission;
    }

    @PrePersist
    void prePersist() {
        createdAt = LocalDateTime.now();
    }
}
