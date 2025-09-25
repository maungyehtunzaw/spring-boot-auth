package dev.yehtun.spring_boot_system.auth.domain.service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import dev.yehtun.spring_boot_system.auth.domain.entity.PermissionEntity;
import dev.yehtun.spring_boot_system.auth.domain.entity.RoleEntity;
import dev.yehtun.spring_boot_system.auth.domain.entity.UserEntity;

/**
 * Service interface for role and permission management operations.
 * 
 * Features:
 * - Role creation and management
 * - Permission assignment and validation
 * - Role hierarchy management
 * - User-role relationship management
 */
public interface RoleService {

    /**
     * Role Management
     */
    RoleEntity createRole(String name, String description);
    RoleEntity createRole(String name, String code, String description);
    RoleEntity updateRole(UUID roleId, String name, String description);
    void deleteRole(UUID roleId);
    Optional<RoleEntity> findById(UUID roleId);
    Optional<RoleEntity> findByName(String name);
    Optional<RoleEntity> findByCode(String code);
    List<RoleEntity> findAllRoles();
    List<RoleEntity> findActiveRoles();
    Page<RoleEntity> findRoles(Pageable pageable);
    
    /**
     * Permission Management
     */
    PermissionEntity createPermission(String name, String description);
    PermissionEntity createPermission(String name, String code, String description, String resource, String action);
    PermissionEntity updatePermission(UUID permissionId, String name, String description);
    void deletePermission(UUID permissionId);
    Optional<PermissionEntity> findPermissionById(UUID permissionId);
    Optional<PermissionEntity> findPermissionByName(String name);
    List<PermissionEntity> findAllPermissions();
    List<PermissionEntity> findActivePermissions();
    Page<PermissionEntity> findPermissions(Pageable pageable);
    
    /**
     * Role-Permission Management
     */
    void assignPermissionToRole(UUID roleId, UUID permissionId);
    void assignPermissionToRole(String roleName, String permissionName);
    void removePermissionFromRole(UUID roleId, UUID permissionId);
    void removePermissionFromRole(String roleName, String permissionName);
    void assignPermissionsToRole(UUID roleId, Set<UUID> permissionIds);
    void replaceRolePermissions(UUID roleId, Set<UUID> permissionIds);
    List<PermissionEntity> getRolePermissions(UUID roleId);
    List<PermissionEntity> getRolePermissions(String roleName);
    boolean roleHasPermission(UUID roleId, String permissionName);
    
    /**
     * User-Role Management
     */
    void assignRoleToUser(UUID userId, UUID roleId);
    void assignRoleToUser(UUID userId, String roleName);
    void removeRoleFromUser(UUID userId, UUID roleId);
    void removeRoleFromUser(UUID userId, String roleName);
    void assignRolesToUser(UUID userId, Set<UUID> roleIds);
    void replaceUserRoles(UUID userId, Set<UUID> roleIds);
    List<RoleEntity> getUserRoles(UUID userId);
    List<UserEntity> getRoleUsers(UUID roleId);
    boolean userHasRole(UUID userId, String roleName);
    
    /**
     * Role Hierarchy
     */
    void setParentRole(UUID childRoleId, UUID parentRoleId);
    void removeParentRole(UUID childRoleId);
    List<RoleEntity> getChildRoles(UUID parentRoleId);
    List<RoleEntity> getRoleHierarchy(UUID roleId);
    boolean isRoleAncestor(UUID ancestorRoleId, UUID descendantRoleId);
    
    /**
     * System Roles
     */
    RoleEntity createSystemRole(String name, String description);
    List<RoleEntity> getSystemRoles();
    List<RoleEntity> getDefaultRoles();
    RoleEntity getGuestRole();
    RoleEntity getUserRole();
    RoleEntity getAdminRole();
    void assignDefaultRoles(UUID userId);
    
    /**
     * Permission Validation
     */
    boolean hasPermission(UUID userId, String permissionName);
    boolean hasPermissions(UUID userId, Set<String> permissionNames);
    boolean hasAnyPermission(UUID userId, Set<String> permissionNames);
    Set<String> getUserPermissions(UUID userId);
    Set<String> getEffectivePermissions(UUID userId);
    void validatePermission(UUID userId, String permissionName);
    
    /**
     * Role Search and Filtering
     */
    Page<RoleEntity> searchRoles(String searchTerm, Pageable pageable);
    Page<PermissionEntity> searchPermissions(String searchTerm, Pageable pageable);
    List<RoleEntity> findRolesByPermission(String permissionName);
    List<PermissionEntity> findPermissionsByCategory(String category);
    List<PermissionEntity> findPermissionsByModule(String module);
    
    /**
     * Statistics and Reporting
     */
    long getRoleCount();
    long getPermissionCount();
    long getUsersWithRoleCount(UUID roleId);
    long getPermissionsInRoleCount(UUID roleId);
    RoleStatistics getRoleStatistics();
    List<RoleUsageStats> getRoleUsageStatistics();
    
    /**
     * Bulk Operations
     */
    void bulkAssignRoleToUsers(UUID roleId, Set<UUID> userIds);
    void bulkRemoveRoleFromUsers(UUID roleId, Set<UUID> userIds);
    void bulkAssignPermissionsToRole(UUID roleId, Set<UUID> permissionIds);
    void bulkRemovePermissionsFromRole(UUID roleId, Set<UUID> permissionIds);
    void bulkActivateRoles(Set<UUID> roleIds);
    void bulkDeactivateRoles(Set<UUID> roleIds);
    
    /**
     * Role Import/Export
     */
    List<RoleEntity> importRoles(List<RoleImportData> importData);
    List<RoleExportData> exportRoles();
    void validateRoleImportData(List<RoleImportData> importData);
    
    /**
     * Role Statistics DTO
     */
    record RoleStatistics(
        long totalRoles,
        long activeRoles,
        long systemRoles,
        long totalPermissions,
        long averagePermissionsPerRole,
        long totalUserRoleAssignments
    ) {}
    
    /**
     * Role Usage Statistics DTO
     */
    record RoleUsageStats(
        UUID roleId,
        String roleName,
        long userCount,
        long permissionCount,
        boolean active
    ) {}
    
    /**
     * Role Import Data DTO
     */
    record RoleImportData(
        String name,
        String code,
        String description,
        boolean active,
        boolean isSystem,
        List<String> permissions
    ) {}
    
    /**
     * Role Export Data DTO
     */
    record RoleExportData(
        UUID id,
        String name,
        String code,
        String description,
        boolean active,
        boolean isSystem,
        List<String> permissions,
        long userCount
    ) {}
}