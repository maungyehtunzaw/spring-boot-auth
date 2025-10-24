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

import dev.yehtun.spring_boot_system.auth.domain.entity.RoleEntity;
import dev.yehtun.spring_boot_system.auth.domain.entity.UserEntity;

/**
 * Repository interface for RoleEntity operations.
 * 
 * Features:
 * - Role management and queries
 * - Permission-based role queries
 * - User-role relationship queries
 * - Role hierarchy and validation
 */
@Repository
public interface RoleRepository extends JpaRepository<RoleEntity, UUID> {

    // Basic finder methods
    Optional<RoleEntity> findByName(String name);
    
    Optional<RoleEntity> findByCode(String code);
    
    boolean existsByName(String name);
    
    boolean existsByCode(String code);
    
    List<RoleEntity> findByIsActiveTrue();
    
    List<RoleEntity> findByIsActiveFalse();

    // User-role relationship queries
    @Query("SELECT r FROM RoleEntity r JOIN r.users u WHERE u = :user")
    List<RoleEntity> findByUser(@Param("user") UserEntity user);
    
    @Query("SELECT r FROM RoleEntity r JOIN r.users u WHERE u.id = :userId")
    List<RoleEntity> findByUserId(@Param("userId") UUID userId);
    
    @Query("SELECT u FROM UserEntity u JOIN u.roles r WHERE r = :role")
    List<UserEntity> findUsersByRole(@Param("role") RoleEntity role);
    
    @Query("SELECT u FROM UserEntity u JOIN u.roles r WHERE r.id = :roleId")
    List<UserEntity> findUsersByRoleId(@Param("roleId") UUID roleId);

    // Permission-based queries
    @Query("SELECT DISTINCT r FROM RoleEntity r JOIN r.permissions p WHERE p.name = :permissionName")
    List<RoleEntity> findByPermissionName(@Param("permissionName") String permissionName);
    
    @Query("SELECT DISTINCT r FROM RoleEntity r JOIN r.permissions p WHERE p.id = :permissionId")
    List<RoleEntity> findByPermissionId(@Param("permissionId") UUID permissionId);
    
    @Query("SELECT DISTINCT r FROM RoleEntity r JOIN r.permissions p WHERE p.name IN :permissionNames")
    List<RoleEntity> findByPermissionNames(@Param("permissionNames") Set<String> permissionNames);

    // System roles
    @Query("SELECT r FROM RoleEntity r WHERE r.isSystemRole = true")
    List<RoleEntity> findSystemRoles();
    
    @Query("SELECT r FROM RoleEntity r WHERE r.isSystemRole = true AND r.name = :name")
    Optional<RoleEntity> findSystemRoleByName(@Param("name") String name);

    // Search and filtering
    @Query("SELECT r FROM RoleEntity r WHERE " +
           "LOWER(r.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(r.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<RoleEntity> searchRoles(@Param("searchTerm") String searchTerm, Pageable pageable);
    
    @Query("SELECT r FROM RoleEntity r WHERE r.isActive = :active")
    Page<RoleEntity> findByActive(@Param("active") boolean active, Pageable pageable);

    // Statistics and counts
    @Query("SELECT COUNT(r) FROM RoleEntity r WHERE r.isActive = true")
    long countActiveRoles();
    
    @Query("SELECT COUNT(r) FROM RoleEntity r WHERE r.isSystemRole = true")
    long countSystemRoles();
    
    @Query("SELECT COUNT(u) FROM UserEntity u JOIN u.roles r WHERE r.id = :roleId")
    long countUsersByRoleId(@Param("roleId") UUID roleId);
    
    @Query("SELECT COUNT(p) FROM PermissionEntity p JOIN p.roles r WHERE r.id = :roleId")
    long countPermissionsByRoleId(@Param("roleId") UUID roleId);

    // Bulk operations
    @Modifying
    @Query("UPDATE RoleEntity r SET r.isActive = :active WHERE r.id IN :roleIds")
    int updateActiveStatus(@Param("roleIds") Set<UUID> roleIds, @Param("active") boolean active);
    
    @Modifying
    @Query("UPDATE RoleEntity r SET r.updatedAt = :timestamp WHERE r.id = :roleId")
    int updateLastModified(@Param("roleId") UUID roleId, @Param("timestamp") LocalDateTime timestamp);

    // Advanced filtering
    @Query("SELECT r FROM RoleEntity r WHERE " +
           "(:name IS NULL OR LOWER(r.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
           "(:description IS NULL OR LOWER(r.description) LIKE LOWER(CONCAT('%', :description, '%'))) AND " +
           "(:active IS NULL OR r.isActive = :active) AND " +
           "(:isSystemRole IS NULL OR r.isSystemRole = :isSystemRole)")
    Page<RoleEntity> findRolesWithFilters(
        @Param("name") String name,
        @Param("description") String description,
        @Param("active") Boolean active,
        @Param("isSystemRole") Boolean isSystemRole,
        Pageable pageable
    );
}