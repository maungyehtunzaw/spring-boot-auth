package dev.yehtun.spring_boot_system.auth.infrastructure.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import dev.yehtun.spring_boot_system.auth.domain.entity.PermissionEntity;
import dev.yehtun.spring_boot_system.auth.domain.entity.RoleEntity;
import dev.yehtun.spring_boot_system.auth.domain.entity.UserEntity;

/**
 * Repository interface for PermissionEntity operations.
 * 
 * Features:
 * - Permission management and queries
 * - Role-permission relationship queries
 * - User permission lookups
 * - Permission grouping and categorization
 */
@Repository
public interface PermissionRepository extends JpaRepository<PermissionEntity, UUID> {

    // Basic finder methods
    Optional<PermissionEntity> findByName(String name);
    
    boolean existsByName(String name);
    
    List<PermissionEntity> findByIsActiveTrue();
    
    List<PermissionEntity> findByIsActiveFalse();

    // Category queries
    List<PermissionEntity> findByCategory(String category);
    
    @Query("SELECT DISTINCT p.category FROM PermissionEntity p WHERE p.category IS NOT NULL ORDER BY p.category")
    List<String> findAllCategories();

    // Role-permission relationship queries
    @Query("SELECT p FROM PermissionEntity p JOIN p.roles r WHERE r = :role")
    List<PermissionEntity> findByRole(@Param("role") RoleEntity role);
    
    @Query("SELECT p FROM PermissionEntity p JOIN p.roles r WHERE r.id = :roleId")
    List<PermissionEntity> findByRoleId(@Param("roleId") UUID roleId);
    
    @Query("SELECT r FROM RoleEntity r JOIN r.permissions p WHERE p = :permission")
    List<RoleEntity> findRolesByPermission(@Param("permission") PermissionEntity permission);
    
    @Query("SELECT r FROM RoleEntity r JOIN r.permissions p WHERE p.id = :permissionId")
    List<RoleEntity> findRolesByPermissionId(@Param("permissionId") UUID permissionId);

    // User permission queries (through roles)
    @Query("SELECT DISTINCT p FROM PermissionEntity p " +
           "JOIN p.roles r JOIN r.users u WHERE u = :user")
    List<PermissionEntity> findByUser(@Param("user") UserEntity user);
    
    @Query("SELECT DISTINCT p FROM PermissionEntity p " +
           "JOIN p.roles r JOIN r.users u WHERE u.id = :userId")
    List<PermissionEntity> findByUserId(@Param("userId") UUID userId);
    
    @Query("SELECT DISTINCT p.name FROM PermissionEntity p " +
           "JOIN p.roles r JOIN r.users u WHERE u.id = :userId")
    Set<String> findPermissionNamesByUserId(@Param("userId") UUID userId);

    // Permission hierarchy and dependencies
    @Query("SELECT p FROM PermissionEntity p WHERE p.parentPermission = :parentPermission")
    List<PermissionEntity> findByParentPermission(@Param("parentPermission") PermissionEntity parentPermission);
    
    @Query("SELECT p FROM PermissionEntity p WHERE p.parentPermission.id = :parentPermissionId")
    List<PermissionEntity> findByParentPermissionId(@Param("parentPermissionId") UUID parentPermissionId);
    
    @Query("SELECT p FROM PermissionEntity p WHERE p.parentPermission IS NULL")
    List<PermissionEntity> findRootPermissions();

    // System permissions
    @Query("SELECT p FROM PermissionEntity p WHERE p.isSystemPermission = true")
    List<PermissionEntity> findSystemPermissions();
    
    @Query("SELECT p FROM PermissionEntity p WHERE p.isSystemPermission = true AND p.name = :name")
    Optional<PermissionEntity> findSystemPermissionByName(@Param("name") String name);

    // Search and filtering
    @Query("SELECT p FROM PermissionEntity p WHERE " +
           "LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<PermissionEntity> searchPermissions(@Param("searchTerm") String searchTerm, Pageable pageable);
    
    @Query("SELECT p FROM PermissionEntity p WHERE p.isActive = :active")
    Page<PermissionEntity> findByActive(@Param("active") boolean active, Pageable pageable);
    
    @Query("SELECT p FROM PermissionEntity p WHERE p.category = :category AND p.isActive = true ORDER BY p.name")
    List<PermissionEntity> findActiveByCategoryOrderByName(@Param("category") String category);

    // Permission validation and checking
    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END " +
           "FROM PermissionEntity p JOIN p.roles r JOIN r.users u " +
           "WHERE u.id = :userId AND p.name = :permissionName")
    boolean hasUserPermission(@Param("userId") UUID userId, @Param("permissionName") String permissionName);
    
    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END " +
           "FROM PermissionEntity p JOIN p.roles r " +
           "WHERE r.id = :roleId AND p.name = :permissionName")
    boolean hasRolePermission(@Param("roleId") UUID roleId, @Param("permissionName") String permissionName);

    // Statistics and counts
    @Query("SELECT COUNT(p) FROM PermissionEntity p WHERE p.isActive = true")
    long countActivePermissions();
    
    @Query("SELECT COUNT(p) FROM PermissionEntity p WHERE p.isSystemPermission = true")
    long countSystemPermissions();
    
    @Query("SELECT COUNT(p) FROM PermissionEntity p WHERE p.category = :category")
    long countByCategory(@Param("category") String category);
    
    @Query("SELECT COUNT(r) FROM RoleEntity r JOIN r.permissions p WHERE p.id = :permissionId")
    long countRolesByPermissionId(@Param("permissionId") UUID permissionId);

    // Bulk operations
    @Modifying
    @Query("UPDATE PermissionEntity p SET p.isActive = :active WHERE p.id IN :permissionIds")
    int updateActiveStatus(@Param("permissionIds") Set<UUID> permissionIds, @Param("active") boolean active);
    
    @Modifying
    @Query("UPDATE PermissionEntity p SET p.updatedAt = :timestamp WHERE p.id = :permissionId")
    int updateLastModified(@Param("permissionId") UUID permissionId, @Param("timestamp") LocalDateTime timestamp);

    // Advanced filtering
    @Query("SELECT p FROM PermissionEntity p WHERE " +
           "(:name IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
           "(:description IS NULL OR LOWER(p.description) LIKE LOWER(CONCAT('%', :description, '%'))) AND " +
           "(:category IS NULL OR p.category = :category) AND " +
           "(:isActive IS NULL OR p.isActive = :isActive) AND " +
           "(:isSystemPermission IS NULL OR p.isSystemPermission = :isSystemPermission)")
    Page<PermissionEntity> findPermissionsWithFilters(
        @Param("name") String name,
        @Param("description") String description,
        @Param("category") String category,
        @Param("isActive") Boolean isActive,
        @Param("isSystemPermission") Boolean isSystemPermission,
        Pageable pageable
    );

    // Permission resource queries
    @Query("SELECT p FROM PermissionEntity p WHERE p.resource = :resource")
    List<PermissionEntity> findByResource(@Param("resource") String resource);
    
    @Query("SELECT p FROM PermissionEntity p WHERE p.action = :action")
    List<PermissionEntity> findByAction(@Param("action") String action);
    
    @Query("SELECT p FROM PermissionEntity p WHERE p.resource = :resource AND p.action = :action")
    Optional<PermissionEntity> findByResourceAndAction(@Param("resource") String resource, @Param("action") String action);
}