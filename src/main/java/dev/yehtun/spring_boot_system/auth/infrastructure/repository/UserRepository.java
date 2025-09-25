package dev.yehtun.spring_boot_system.auth.infrastructure.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import dev.yehtun.spring_boot_system.auth.domain.entity.UserEntity;
import dev.yehtun.spring_boot_system.auth.domain.enums.UserStatus;
import dev.yehtun.spring_boot_system.auth.domain.enums.UserType;

/**
 * Repository interface for UserEntity operations.
 * 
 * Features:
 * - Guest user management
 * - Authentication queries
 * - User lifecycle management
 * - Security and cleanup operations
 */
@Repository
public interface UserRepository extends JpaRepository<UserEntity, UUID> {

    // Basic finder methods
    Optional<UserEntity> findByUsername(String username);
    
    Optional<UserEntity> findByEmail(String email);
    
    Optional<UserEntity> findByUsernameOrEmail(String username, String email);
    
    boolean existsByUsername(String username);
    
    boolean existsByEmail(String email);

    // Guest user specific queries
    @Query("SELECT u FROM UserEntity u WHERE u.userType = :userType")
    List<UserEntity> findByUserType(@Param("userType") UserType userType);
    
    @Query("SELECT u FROM UserEntity u WHERE u.userType = 'GUEST' AND u.createdAt < :cutoffDate")
    List<UserEntity> findExpiredGuestUsers(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    @Query("SELECT COUNT(u) FROM UserEntity u WHERE u.userType = 'GUEST'")
    long countGuestUsers();

    // User status queries
    List<UserEntity> findByUserStatus(UserStatus userStatus);
    
    @Query("SELECT u FROM UserEntity u WHERE u.userStatus = :status AND u.userType != 'GUEST'")
    List<UserEntity> findNonGuestUsersByStatus(@Param("status") UserStatus status);
    
    @Query("SELECT u FROM UserEntity u WHERE u.userStatus = 'LOCKED' AND u.lockedUntil < :now")
    List<UserEntity> findUsersWithExpiredLocks(@Param("now") LocalDateTime now);

    // Authentication and security queries
    @Query("SELECT u FROM UserEntity u WHERE u.username = :identifier OR u.email = :identifier")
    Optional<UserEntity> findByUsernameOrEmailForAuthentication(@Param("identifier") String identifier);
    
    @Query("SELECT u FROM UserEntity u WHERE (u.username = :identifier OR u.email = :identifier) AND u.userStatus = 'ACTIVE'")
    Optional<UserEntity> findActiveUserByUsernameOrEmail(@Param("identifier") String identifier);
    
    @Query("SELECT u FROM UserEntity u WHERE u.email = :email AND u.emailVerified = true")
    Optional<UserEntity> findByVerifiedEmail(@Param("email") String email);

    // User lifecycle queries
    @Query("SELECT u FROM UserEntity u WHERE u.lastLoginAt < :cutoffDate AND u.userType != 'GUEST'")
    List<UserEntity> findInactiveUsers(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    @Query("SELECT u FROM UserEntity u WHERE u.passwordChangedAt < :cutoffDate AND u.userType != 'GUEST'")
    List<UserEntity> findUsersWithExpiredPasswords(@Param("cutoffDate") LocalDateTime cutoffDate);

    // Pagination queries
    Page<UserEntity> findByUserTypeAndUserStatus(UserType userType, UserStatus userStatus, Pageable pageable);
    
    @Query("SELECT u FROM UserEntity u WHERE u.userType != 'GUEST' ORDER BY u.createdAt DESC")
    Page<UserEntity> findNonGuestUsers(Pageable pageable);

    // Update operations
    @Modifying
    @Query("UPDATE UserEntity u SET u.lastLoginAt = :loginTime WHERE u.id = :userId")
    void updateLastLoginTime(@Param("userId") UUID userId, @Param("loginTime") LocalDateTime loginTime);
    
    @Modifying
    @Query("UPDATE UserEntity u SET u.loginAttempts = :attempts WHERE u.id = :userId")
    void updateLoginAttempts(@Param("userId") UUID userId, @Param("attempts") Integer attempts);
    
    @Modifying
    @Query("UPDATE UserEntity u SET u.userStatus = :status, u.lockedUntil = :lockedUntil WHERE u.id = :userId")
    void lockUser(@Param("userId") UUID userId, @Param("status") UserStatus status, @Param("lockedUntil") LocalDateTime lockedUntil);
    
    @Modifying
    @Query("UPDATE UserEntity u SET u.userStatus = 'ACTIVE', u.lockedUntil = null, u.loginAttempts = 0 WHERE u.id = :userId")
    void unlockUser(@Param("userId") UUID userId);
    
    @Modifying
    @Query("UPDATE UserEntity u SET u.emailVerified = true, u.emailVerifiedAt = :verifiedAt WHERE u.id = :userId")
    void markEmailAsVerified(@Param("userId") UUID userId, @Param("verifiedAt") LocalDateTime verifiedAt);

    // Cleanup operations
    @Modifying
    @Query("DELETE FROM UserEntity u WHERE u.userType = 'GUEST' AND u.createdAt < :cutoffDate")
    int deleteExpiredGuestUsers(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    @Modifying
    @Query("UPDATE UserEntity u SET u.userStatus = 'INACTIVE' WHERE u.lastLoginAt < :cutoffDate AND u.userType != 'GUEST' AND u.userStatus = 'ACTIVE'")
    int markInactiveUsers(@Param("cutoffDate") LocalDateTime cutoffDate);

    // Statistics queries
    @Query("SELECT COUNT(u) FROM UserEntity u WHERE u.userType != 'GUEST'")
    long countRegisteredUsers();
    
    @Query("SELECT COUNT(u) FROM UserEntity u WHERE u.userType != 'GUEST' AND u.userStatus = 'ACTIVE'")
    long countActiveUsers();
    
    @Query("SELECT COUNT(u) FROM UserEntity u WHERE u.createdAt >= :fromDate")
    long countUsersCreatedSince(@Param("fromDate") LocalDateTime fromDate);
    
    @Query("SELECT COUNT(u) FROM UserEntity u WHERE u.lastLoginAt >= :fromDate")
    long countUsersLoggedInSince(@Param("fromDate") LocalDateTime fromDate);

    // Advanced search queries
    @Query("SELECT u FROM UserEntity u WHERE " +
           "(:username IS NULL OR LOWER(u.username) LIKE LOWER(CONCAT('%', :username, '%'))) AND " +
           "(:email IS NULL OR LOWER(u.email) LIKE LOWER(CONCAT('%', :email, '%'))) AND " +
           "(:userType IS NULL OR u.userType = :userType) AND " +
           "(:userStatus IS NULL OR u.userStatus = :userStatus)")
    Page<UserEntity> findUsersWithFilters(
        @Param("username") String username,
        @Param("email") String email, 
        @Param("userType") UserType userType,
        @Param("userStatus") UserStatus userStatus,
        Pageable pageable
    );
}