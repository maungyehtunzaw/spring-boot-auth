package dev.yehtun.spring_boot_system.auth.domain.entity;

import java.util.HashSet;
import java.util.Set;

import dev.yehtun.spring_boot_system.shared.domain.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Permission entity representing system permissions.
 * 
 * Features:
 * - Resource-action based permission model
 * - Fine-grained access control
 * - Permission categorization
 * - Audit trail tracking
 */
@Entity
@Table(name = "permissions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PermissionEntity extends AuditableEntity {

    @Column(name = "name", length = 100, nullable = false, unique = true)
    private String name;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "resource", length = 100, nullable = false)
    private String resource;

    @Column(name = "action", length = 50, nullable = false)
    private String action;

    @Column(name = "category", length = 50)
    private String category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_permission_id")
    private PermissionEntity parentPermission;

    @OneToMany(mappedBy = "parentPermission", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private Set<PermissionEntity> childPermissions = new HashSet<>();

    @Column(name = "is_system_permission", nullable = false)
    @Builder.Default
    private Boolean isSystemPermission = false;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @ManyToMany(mappedBy = "permissions", fetch = FetchType.LAZY)
    @Builder.Default
    private Set<RoleEntity> roles = new HashSet<>();

    // Business Logic Methods

    /**
     * Create a permission with resource and action
     */
    public static PermissionEntity create(String resource, String action, String description) {
        String permissionName = resource.toUpperCase() + "_" + action.toUpperCase();
        return PermissionEntity.builder()
                .name(permissionName)
                .resource(resource)
                .action(action)
                .description(description)
                .isActive(true)
                .isSystemPermission(false)
                .build();
    }

    /**
     * Create a system permission (cannot be deleted)
     */
    public static PermissionEntity createSystemPermission(String resource, String action, String description) {
        PermissionEntity permission = create(resource, action, description);
        permission.setIsSystemPermission(true);
        return permission;
    }

    /**
     * Check if this permission matches resource and action
     */
    public boolean matches(String resource, String action) {
        return this.resource.equals(resource) && this.action.equals(action);
    }

    /**
     * Get full permission identifier (resource:action)
     */
    public String getFullIdentifier() {
        return resource + ":" + action;
    }

    /**
     * Check if this permission can be deleted
     */
    public boolean canBeDeleted() {
        return !isSystemPermission && (roles == null || roles.isEmpty());
    }

    /**
     * Get the number of roles that have this permission
     */
    public int getRoleCount() {
        return roles != null ? roles.size() : 0;
    }

    /**
     * Activate this permission
     */
    public void activate() {
        this.isActive = true;
    }

    /**
     * Deactivate this permission
     */
    public void deactivate() {
        if (!isSystemPermission) {
            this.isActive = false;
        }
    }
}