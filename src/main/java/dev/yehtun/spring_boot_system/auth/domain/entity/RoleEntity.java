package dev.yehtun.spring_boot_system.auth.domain.entity;

import java.util.HashSet;
import java.util.Set;

import dev.yehtun.spring_boot_system.shared.domain.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Role entity representing user roles in the system.
 * 
 * Features:
 * - Role-based access control
 * - Permission aggregation
 * - Business logic for role management
 * - Audit trail tracking
 */
@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class RoleEntity extends AuditableEntity {

    @Column(name = "name", length = 50, nullable = false, unique = true)
    private String name;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "is_system_role", nullable = false)
    @Builder.Default
    private Boolean isSystemRole = false;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @ManyToMany(mappedBy = "roles", fetch = FetchType.LAZY)
    @Builder.Default
    private Set<UserEntity> users = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "role_permissions",
        joinColumns = @JoinColumn(name = "role_id"),
        inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    @Builder.Default
    private Set<PermissionEntity> permissions = new HashSet<>();

    // Business Logic Methods

    /**
     * Add a permission to this role
     */
    public void addPermission(PermissionEntity permission) {
        if (permission != null) {
            this.permissions.add(permission);
        }
    }

    /**
     * Remove a permission from this role
     */
    public void removePermission(PermissionEntity permission) {
        if (permission != null) {
            this.permissions.remove(permission);
        }
    }

    /**
     * Check if this role has a specific permission
     */
    public boolean hasPermission(String permissionName) {
        return permissions.stream()
                .anyMatch(permission -> permission.getName().equals(permissionName));
    }

    /**
     * Check if this role has permission for a resource-action combination
     */
    public boolean hasPermission(String resource, String action) {
        return permissions.stream()
                .anyMatch(permission -> 
                    permission.getResource().equals(resource) && 
                    permission.getAction().equals(action));
    }

    /**
     * Get the total number of users with this role
     */
    public int getUserCount() {
        return users != null ? users.size() : 0;
    }

    /**
     * Get the total number of permissions for this role
     */
    public int getPermissionCount() {
        return permissions != null ? permissions.size() : 0;
    }

    /**
     * Check if this role can be deleted (not system role and no active users)
     */
    public boolean canBeDeleted() {
        return !isSystemRole && getUserCount() == 0;
    }

    /**
     * Activate this role
     */
    public void activate() {
        this.isActive = true;
    }

    /**
     * Deactivate this role (users keep the role but it becomes inactive)
     */
    public void deactivate() {
        if (!isSystemRole) {
            this.isActive = false;
        }
    }
}