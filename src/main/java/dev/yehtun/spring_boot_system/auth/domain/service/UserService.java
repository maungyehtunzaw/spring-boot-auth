package dev.yehtun.spring_boot_system.auth.domain.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import dev.yehtun.spring_boot_system.auth.domain.entity.RoleEntity;
import dev.yehtun.spring_boot_system.auth.domain.entity.UserEntity;
import dev.yehtun.spring_boot_system.auth.domain.enums.UserStatus;
import dev.yehtun.spring_boot_system.auth.domain.enums.UserType;

/**
 * Service interface for user management operations.
 * 
 * Features:
 * - User profile management
 * - User search and filtering
 * - Role and permission management
 * - User lifecycle operations
 * - Statistics and reporting
 */
public interface UserService {

    /**
     * User Profile Management
     */
    UserEntity createUser(String username, String email, String password);
    UserEntity createUser(String username, String email, String password, String firstName, String lastName);
    UserEntity updateProfile(UUID userId, String firstName, String lastName, String email);
    UserEntity updateUsername(UUID userId, String newUsername);
    UserEntity updateEmail(UUID userId, String newEmail);
    void deleteUser(UUID userId);
    void softDeleteUser(UUID userId);
    Optional<UserEntity> findById(UUID userId);
    Optional<UserEntity> findByUsername(String username);
    Optional<UserEntity> findByEmail(String email);
    
    /**
     * User Search and Filtering
     */
    Page<UserEntity> findAll(Pageable pageable);
    Page<UserEntity> findByUserType(UserType userType, Pageable pageable);
    Page<UserEntity> findByUserStatus(UserStatus userStatus, Pageable pageable);
    Page<UserEntity> searchUsers(String searchTerm, Pageable pageable);
    Page<UserEntity> findUsersWithFilters(
        String username, 
        String email, 
        UserType userType, 
        UserStatus userStatus, 
        Pageable pageable
    );
    List<UserEntity> findActiveUsers();
    List<UserEntity> findInactiveUsers();
    List<UserEntity> findUsersByRole(String roleName);
    
    /**
     * Role and Permission Management
     */
    void assignRole(UUID userId, String roleName);
    void assignRole(UUID userId, UUID roleId);
    void removeRole(UUID userId, String roleName);
    void removeRole(UUID userId, UUID roleId);
    void assignRoles(UUID userId, List<String> roleNames);
    void replaceRoles(UUID userId, List<String> roleNames);
    List<RoleEntity> getUserRoles(UUID userId);
    boolean hasRole(UUID userId, String roleName);
    boolean hasPermission(UUID userId, String permissionName);
    List<String> getUserPermissions(UUID userId);
    
    /**
     * User Status Management
     */
    void activateUser(UUID userId);
    void deactivateUser(UUID userId);
    void suspendUser(UUID userId, String reason);
    void lockUser(UUID userId, String reason);
    void unlockUser(UUID userId);
    void banUser(UUID userId, String reason);
    void unbanUser(UUID userId);
    UserStatus getUserStatus(UUID userId);
    boolean isUserActive(UUID userId);
    boolean isUserLocked(UUID userId);
    boolean isUserSuspended(UUID userId);
    
    /**
     * User Lifecycle Operations
     */
    void promoteGuestUser(UUID guestUserId, String username, String email, String password);
    void convertUserType(UUID userId, UserType newUserType);
    void mergeUsers(UUID sourceUserId, UUID targetUserId);
    void archiveUser(UUID userId);
    void restoreUser(UUID userId);
    void permanentlyDeleteUser(UUID userId);
    
    /**
     * User Validation
     */
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    boolean isUsernameAvailable(String username);
    boolean isEmailAvailable(String email);
    boolean validateUserData(String username, String email, String password);
    List<String> getValidationErrors(String username, String email, String password);
    
    /**
     * User Activity Tracking
     */
    void updateLastLogin(UUID userId);
    void updateLastActivity(UUID userId);
    LocalDateTime getLastLoginTime(UUID userId);
    LocalDateTime getLastActivityTime(UUID userId);
    List<UserEntity> findRecentlyActiveUsers(LocalDateTime since);
    List<UserEntity> findInactiveUsersSince(LocalDateTime cutoffDate);
    
    /**
     * Guest User Management
     */
    List<UserEntity> findGuestUsers();
    List<UserEntity> findExpiredGuestUsers();
    void cleanupExpiredGuestUsers();
    long countGuestUsers();
    void convertExpiredGuestsToInactive();
    
    /**
     * Statistics and Reporting
     */
    long getTotalUserCount();
    long getActiveUserCount();
    long getUserCountByType(UserType userType);
    long getUserCountByStatus(UserStatus userStatus);
    long getNewUserCountSince(LocalDateTime since);
    long getActiveUserCountSince(LocalDateTime since);
    UserStatistics getUserStatistics();
    List<UserEntity> getTopActiveUsers(int limit);
    List<UserEntity> getRecentRegistrations(int limit);
    
    /**
     * Bulk Operations
     */
    void bulkActivateUsers(List<UUID> userIds);
    void bulkDeactivateUsers(List<UUID> userIds);
    void bulkAssignRole(List<UUID> userIds, String roleName);
    void bulkRemoveRole(List<UUID> userIds, String roleName);
    void bulkUpdateStatus(List<UUID> userIds, UserStatus status);
    void bulkDeleteUsers(List<UUID> userIds);
    
    /**
     * User Import/Export
     */
    List<UserEntity> importUsers(List<UserImportData> importData);
    List<UserExportData> exportUsers(List<UUID> userIds);
    List<UserExportData> exportAllUsers();
    void validateImportData(List<UserImportData> importData);
    
    /**
     * User Statistics DTO
     */
    record UserStatistics(
        long totalUsers,
        long activeUsers,
        long inactiveUsers,
        long guestUsers,
        long adminUsers,
        long newUsersThisMonth,
        long activeUsersThisMonth
    ) {}
    
    /**
     * User Import Data DTO
     */
    record UserImportData(
        String username,
        String email,
        String password,
        String firstName,
        String lastName,
        UserType userType,
        UserStatus userStatus,
        List<String> roles
    ) {}
    
    /**
     * User Export Data DTO
     */
    record UserExportData(
        UUID id,
        String username,
        String email,
        String firstName,
        String lastName,
        UserType userType,
        UserStatus userStatus,
        LocalDateTime createdAt,
        LocalDateTime lastLoginAt,
        List<String> roles
    ) {}
}